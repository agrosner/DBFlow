package com.dbflow5.processor

import com.dbflow5.processor.definition.DatabaseDefinition
import com.dbflow5.processor.definition.DatabaseHolderDefinition
import com.dbflow5.processor.definition.DatabaseObjectHolder
import com.dbflow5.processor.definition.DeclarativeDatabaseObjectHolder
import com.dbflow5.processor.definition.EntityDefinition
import com.dbflow5.processor.definition.ManyToManyDefinition
import com.dbflow5.processor.definition.MigrationDefinition
import com.dbflow5.processor.definition.ModelViewDefinition
import com.dbflow5.processor.definition.QueryModelDefinition
import com.dbflow5.processor.definition.TableDefinition
import com.dbflow5.processor.definition.TypeConverterDefinition
import com.dbflow5.processor.definition.UniqueDatabaseObjectHolder
import com.dbflow5.processor.definition.safeWritePackageHelper
import com.dbflow5.processor.utils.writeBaseDefinition
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeName
import java.io.IOException
import javax.annotation.processing.FilerException
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic
import kotlin.reflect.KClass

/**
 * Description: The main object graph during processing. This class collects all of the
 * processor classes and writes them to the corresponding database holders.
 */
class ProcessorManager internal constructor(val processingEnvironment: ProcessingEnvironment) :
    Handler {

    companion object {
        lateinit var manager: ProcessorManager
    }

    private val uniqueDatabases = arrayListOf<TypeName>()
    private val modelToDatabaseMap = hashMapOf<TypeName, TypeName>()
    val typeConverters = linkedMapOf<TypeName?, TypeConverterDefinition>()
    private val databaseDefinitionMap = hashMapOf<TypeName?, DatabaseObjectHolder>()
    private val handlers = mutableSetOf<AnnotatedHandler<*>>()

    init {
        manager = this
    }

    fun addHandlers(vararg containerHandlers: AnnotatedHandler<*>) {
        containerHandlers.forEach { handlers.add(it) }
    }

    val messager: Messager = processingEnvironment.messager

    val typeUtils: Types = processingEnvironment.typeUtils

    val elements: Elements = processingEnvironment.elementUtils

    fun addDatabase(database: TypeName) {
        if (!uniqueDatabases.contains(database)) {
            uniqueDatabases.add(database)
        }
    }

    fun addDatabaseDefinition(databaseDefinition: DatabaseDefinition) {
        val holderDefinition = getOrPutDatabase(databaseDefinition.elementClassName)
        holderDefinition.databaseDefinition = databaseDefinition
    }

    fun getDatabaseHolderDefinitionList() = databaseDefinitionMap.values.toList()

    fun getDatabaseHolderDefinition(databaseName: TypeName?) = databaseDefinitionMap[databaseName]

    fun addTypeConverterDefinition(definition: TypeConverterDefinition) {
        typeConverters[definition.modelTypeName] = definition
    }

    fun getTypeConverterDefinition(typeName: TypeName?): TypeConverterDefinition? =
        typeConverters[typeName]

    fun addModelToDatabase(modelType: TypeName?, databaseName: TypeName) {
        modelType?.let { type ->
            addDatabase(databaseName)
            modelToDatabaseMap[type] = databaseName
        }
    }

    fun addQueryModelDefinition(queryModelDefinition: QueryModelDefinition) {
        queryModelDefinition.elementClassName?.let {
            getOrPutDatabase(queryModelDefinition.associationalBehavior.databaseTypeName)
                .putQueryModel(queryModelDefinition, this)
        }
    }

    fun addTableDefinition(tableDefinition: TableDefinition) {
        tableDefinition.elementClassName?.let {
            val holderDefinition =
                getOrPutDatabase(tableDefinition.associationalBehavior.databaseTypeName)
            holderDefinition.putTable(tableDefinition, this)
        }
    }

    fun addManyToManyDefinition(manyToManyDefinition: ManyToManyDefinition) {
        val databaseHolderDefinition = getOrPutDatabase(manyToManyDefinition.databaseTypeName)
        databaseHolderDefinition.putManyToMany(manyToManyDefinition, this)
    }

    fun getTableDefinition(databaseName: TypeName?, typeName: TypeName?): TableDefinition? {
        return getOrPutDatabase(databaseName).table(typeName)
    }

    fun getQueryModelDefinition(
        databaseName: TypeName?,
        typeName: TypeName?
    ): QueryModelDefinition? {
        return getOrPutDatabase(databaseName).queryModel(typeName)
    }

    fun getModelViewDefinition(databaseName: TypeName?, typeName: TypeName?): ModelViewDefinition? {
        return getOrPutDatabase(databaseName).modelView(typeName)
    }

    fun getReferenceDefinition(databaseName: TypeName?, typeName: TypeName?): EntityDefinition? {
        return getTableDefinition(databaseName, typeName)
            ?: getQueryModelDefinition(databaseName, typeName)
            ?: getModelViewDefinition(databaseName, typeName)
    }

    fun addModelViewDefinition(modelViewDefinition: ModelViewDefinition) {
        modelViewDefinition.elementClassName?.let {
            getOrPutDatabase(modelViewDefinition.associationalBehavior.databaseTypeName)
                .putModelView(modelViewDefinition, this)
        }
    }

    fun getTypeConverters() =
        typeConverters.values.toHashSet().sortedBy { it.modelTypeName?.toString() }

    fun getTableDefinitions(databaseName: TypeName): List<TableDefinition> {
        val databaseHolderDefinition = getOrPutDatabase(databaseName)
        return databaseHolderDefinition.tables
    }

    fun setTableDefinitions(
        tableDefinitionSet: MutableMap<TypeName, TableDefinition>,
        databaseName: TypeName
    ) {
        getOrPutDatabase(databaseName).setTableDefinitions(tableDefinitionSet)
    }

    fun getModelViewDefinitions(databaseName: TypeName): List<ModelViewDefinition> {
        return getOrPutDatabase(databaseName).modelViews
    }

    fun setModelViewDefinitions(
        modelViewDefinitionMap: MutableMap<TypeName, ModelViewDefinition>,
        elementClassName: ClassName
    ) {
        val databaseDefinition = getOrPutDatabase(elementClassName)
        databaseDefinition.setModelViewDefinitions(modelViewDefinitionMap)
    }

    fun getQueryModelDefinitions(databaseName: TypeName): List<QueryModelDefinition> {
        return getOrPutDatabase(databaseName).queryModels
    }

    fun addMigrationDefinition(migrationDefinition: MigrationDefinition) {
        getOrPutDatabase(migrationDefinition.databaseName).putMigrationDefinition(
            migrationDefinition,
            this
        )
    }

    fun getMigrationsForDatabase(databaseName: TypeName) =
        getOrPutDatabase(databaseName).migrations

    fun logError(callingClass: KClass<*>?, error: String?, vararg args: Any?) {
        messager.printMessage(
            Diagnostic.Kind.ERROR,
            String.format(
                "${
                    (callingClass?.toString() ?: "")
                        // don't print this in logs.
                        .replace("(Kotlin reflection is not available)", "")
                } : ${error?.trim()}", *args
            )
        )
    }

    fun logError(error: String?) = logError(callingClass = null, error = error)

    fun logWarning(error: String?) {
        messager.printMessage(Diagnostic.Kind.WARNING, error ?: "")
    }

    fun logWarning(callingClass: KClass<*>, error: String) {
        logWarning("$callingClass : $error")
    }

    private fun getOrPutDatabase(databaseName: TypeName?): DatabaseObjectHolder =
        databaseDefinitionMap.getOrPut(databaseName) {
            if (databaseName == null || databaseName == TypeName.OBJECT) {
                DeclarativeDatabaseObjectHolder()
            } else {
                UniqueDatabaseObjectHolder()
            }
        }

    override fun handle(processorManager: ProcessorManager, roundEnvironment: RoundEnvironment) {
        handlers.forEach { it.handle(processorManager, roundEnvironment) }

        val nullHolder = getDatabaseHolderDefinitionList().first { it.databaseDefinition == null }
        val databaseDefinitions = getDatabaseHolderDefinitionList()
            .filter { it.databaseDefinition != null }
            .sortedBy { it.databaseDefinition?.outputClassName?.simpleName() }
            .onEach { db ->
                // patch any declared objects
                val definition = db.databaseDefinition!!
                definition.declaredTables.forEach { table ->
                    db.putTable(
                        nullHolder.tables.firstOrNull { it.elementClassName == table }
                            ?: throw IllegalStateException(
                                "Floating $table reference not found. Is it marked with @Table?"
                            ),
                        this,
                    )
                    // any many to many, add table also part of DB.
                    nullHolder.manyToManys.find { it.referencedTable == table }
                        ?.let { manyDefinition ->
                            nullHolder.tables.find { it.elementClassName == manyDefinition.outputClassName }
                                ?.let { table ->
                                    db.putTable(table, this)
                                }
                            db.putManyToMany(manyDefinition, this)
                        }
                }
                definition.declaredQueries.forEach { query ->
                    db.putQueryModel(
                        nullHolder.queryModels.firstOrNull { it.elementClassName == query }
                            ?: throw IllegalStateException(
                                "Floating $query reference not found. Is it marked with @Query?" +
                                    "" +
                                    "${nullHolder}: ${nullHolder.databaseDefinition}"
                            ),
                        this,
                    )
                }
                definition.declaredViews.forEach { view ->
                    db.putModelView(
                        nullHolder.modelViews.firstOrNull { it.elementClassName == view }
                            ?: throw IllegalStateException(
                                "Floating $view reference not found. Is it marked with @ModelView?"
                            ),
                        this,
                    )
                }
                definition.declaredMigrations.forEach { migration ->
                    db.putMigrationDefinition(
                        nullHolder.migrations.values.flatten()
                            .firstOrNull { it.elementClassName == migration }
                            ?: throw IllegalStateException(
                                "Floating $migration reference not found. Is it marked with @Migration?"
                            ),
                        this,
                    )
                }
            }
        for (databaseHolderDefinition in databaseDefinitions) {
            try {
                if (databaseHolderDefinition.databaseDefinition == null) {
                    manager.logError(databaseHolderDefinition.getMissingDBRefs().joinToString("\n"))
                    continue
                }
                val manyToManyDefinitions = databaseHolderDefinition.manyToManys

                val flattenedList =
                    manyToManyDefinitions.sortedBy { it.outputClassName?.simpleName() }
                for (manyToManyList in flattenedList) {
                    manyToManyList.prepareForWrite()
                    logWarning("Writing many to many ${manyToManyList.outputClassName}")
                    manyToManyList.writeBaseDefinition(processorManager)
                }

                // process all in next round.
                if (manyToManyDefinitions.isNotEmpty()) {
                    databaseHolderDefinition.clearManyToMany()
                    continue
                }

                databaseHolderDefinition.databaseDefinition?.validateAndPrepareToWrite()

                if (roundEnvironment.processingOver()) {
                    databaseHolderDefinition.databaseDefinition?.let {
                        if (it.outputClassName != null) {
                            JavaFile.builder(it.packageName, it.typeSpec).build()
                                .writeTo(processorManager.processingEnvironment.filer)
                        }
                    }
                }

                val tableDefinitions = databaseHolderDefinition.tables
                    .sortedBy { it.outputClassName?.simpleName() }

                tableDefinitions.forEach { it.writeBaseDefinition(processorManager) }

                val modelViewDefinitions = databaseHolderDefinition.modelViews
                modelViewDefinitions
                    .sortedByDescending { it.priority }
                    .forEach { it.writeBaseDefinition(processorManager) }

                val queryModelDefinitions = databaseHolderDefinition.queryModels
                    .sortedBy { it.outputClassName?.simpleName() }
                queryModelDefinitions.forEach { it.writeBaseDefinition(processorManager) }

                tableDefinitions.safeWritePackageHelper(processorManager)
                modelViewDefinitions.safeWritePackageHelper(processorManager)
                queryModelDefinitions.safeWritePackageHelper(processorManager)
            } catch (e: IOException) {
            }

        }

        try {
            val databaseHolderDefinition = DatabaseHolderDefinition(processorManager)
            if (!databaseHolderDefinition.isGarbage()) {
                JavaFile.builder(
                    ClassNames.FLOW_MANAGER_PACKAGE,
                    databaseHolderDefinition.typeSpec
                ).build()
                    .writeTo(processorManager.processingEnvironment.filer)
            }
        } catch (e: FilerException) {
        } catch (e: IOException) {
            logError(e.message)
        }
    }

    fun elementBelongsInTable(element: Element): Boolean {
        val enclosingElement = element.enclosingElement
        var find: EntityDefinition? =
            databaseDefinitionMap.values.flatMap { it.tables }
                .find { it.element == enclosingElement }

        // modelview check.
        if (find == null) {
            find = databaseDefinitionMap.values.flatMap { it.modelViews }
                .find { it.element == enclosingElement }
        }
        // querymodel check
        if (find == null) {
            find = databaseDefinitionMap.values.flatMap { it.queryModels }
                .find { it.element == enclosingElement }
        }
        return find != null
    }

}
