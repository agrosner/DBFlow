package com.dbflow5.processor

import com.dbflow5.processor.definition.DatabaseDefinition
import com.dbflow5.processor.definition.DatabaseHolderDefinition
import com.dbflow5.processor.definition.DatabaseObjectHolder
import com.dbflow5.processor.definition.EntityDefinition
import com.dbflow5.processor.definition.ManyToManyDefinition
import com.dbflow5.processor.definition.MigrationDefinition
import com.dbflow5.processor.definition.ModelViewDefinition
import com.dbflow5.processor.definition.QueryModelDefinition
import com.dbflow5.processor.definition.TableDefinition
import com.dbflow5.processor.definition.TypeConverterDefinition
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
    private val migrations =
        hashMapOf<TypeName?, MutableMap<Int, MutableList<MigrationDefinition>>>()

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
        holderDefinition?.databaseDefinition = databaseDefinition
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
                ?.queryModelDefinitionMap?.put(it, queryModelDefinition)
        }
    }

    fun addTableDefinition(tableDefinition: TableDefinition) {
        tableDefinition.elementClassName?.let {
            val holderDefinition =
                getOrPutDatabase(tableDefinition.associationalBehavior.databaseTypeName)
            holderDefinition?.tableDefinitionMap?.put(it, tableDefinition)
            holderDefinition?.tableNameMap?.let {
                val tableName = tableDefinition.associationalBehavior.name
                if (holderDefinition.tableNameMap.containsKey(tableName)) {
                    logError(
                        "Found duplicate table $tableName " +
                            "for database ${holderDefinition.databaseDefinition?.elementName}"
                    )
                } else {
                    holderDefinition.tableNameMap.put(tableName, tableDefinition)
                }
            }
        }
    }

    fun addManyToManyDefinition(manyToManyDefinition: ManyToManyDefinition) {
        val databaseHolderDefinition = getOrPutDatabase(manyToManyDefinition.databaseTypeName)
        databaseHolderDefinition?.manyToManyDefinitionMap?.let {
            manyToManyDefinition.elementClassName?.let { elementClassName ->
                it.getOrPut(elementClassName) { arrayListOf() }
                    .add(manyToManyDefinition)
            }
        }
    }

    fun getTableDefinition(databaseName: TypeName?, typeName: TypeName?): TableDefinition? {
        return getOrPutDatabase(databaseName)?.tableDefinitionMap?.get(typeName)
    }

    fun getQueryModelDefinition(
        databaseName: TypeName?,
        typeName: TypeName?
    ): QueryModelDefinition? {
        return getOrPutDatabase(databaseName)?.queryModelDefinitionMap?.get(typeName)
    }

    fun getModelViewDefinition(databaseName: TypeName?, typeName: TypeName?): ModelViewDefinition? {
        return getOrPutDatabase(databaseName)?.modelViewDefinitionMap?.get(typeName)
    }

    fun getReferenceDefinition(databaseName: TypeName?, typeName: TypeName?): EntityDefinition? {
        return getTableDefinition(databaseName, typeName)
            ?: getQueryModelDefinition(databaseName, typeName)
            ?: getModelViewDefinition(databaseName, typeName)
    }

    fun addModelViewDefinition(modelViewDefinition: ModelViewDefinition) {
        modelViewDefinition.elementClassName?.let {
            getOrPutDatabase(modelViewDefinition.associationalBehavior.databaseTypeName)
                ?.modelViewDefinitionMap?.put(it, modelViewDefinition)
        }
    }

    fun getTypeConverters() =
        typeConverters.values.toHashSet().sortedBy { it.modelTypeName?.toString() }

    fun getTableDefinitions(databaseName: TypeName): List<TableDefinition> {
        val databaseHolderDefinition = getOrPutDatabase(databaseName)
        return (databaseHolderDefinition?.tableDefinitionMap?.values ?: arrayListOf())
            .toHashSet()
            .sortedBy { it.outputClassName?.simpleName() }
    }

    fun setTableDefinitions(
        tableDefinitionSet: MutableMap<TypeName, TableDefinition>,
        databaseName: TypeName
    ) {
        val databaseDefinition = getOrPutDatabase(databaseName)
        databaseDefinition?.tableDefinitionMap = tableDefinitionSet
    }

    fun getModelViewDefinitions(databaseName: TypeName): List<ModelViewDefinition> {
        val databaseDefinition = getOrPutDatabase(databaseName)
        return (databaseDefinition?.modelViewDefinitionMap?.values ?: arrayListOf())
            .toHashSet()
            .sortedBy { it.outputClassName?.simpleName() }
            .sortedByDescending { it.priority }
    }

    fun setModelViewDefinitions(
        modelViewDefinitionMap: MutableMap<TypeName, ModelViewDefinition>,
        elementClassName: ClassName
    ) {
        val databaseDefinition = getOrPutDatabase(elementClassName)
        databaseDefinition?.modelViewDefinitionMap = modelViewDefinitionMap
    }

    fun getQueryModelDefinitions(databaseName: TypeName): List<QueryModelDefinition> {
        val databaseDefinition = getOrPutDatabase(databaseName)
        return (databaseDefinition?.queryModelDefinitionMap?.values ?: arrayListOf())
            .toHashSet()
            .sortedBy { it.outputClassName?.simpleName() }
    }

    fun addMigrationDefinition(migrationDefinition: MigrationDefinition) {
        val migrationDefinitionMap =
            migrations.getOrPut(migrationDefinition.databaseName) { hashMapOf() }
        val migrationDefinitions =
            migrationDefinitionMap.getOrPut(migrationDefinition.version) { arrayListOf() }
        if (!migrationDefinitions.contains(migrationDefinition)) {
            migrationDefinitions.add(migrationDefinition)
        }
    }

    fun getMigrationsForDatabase(databaseName: TypeName) = migrations[databaseName]
        ?: hashMapOf<Int, List<MigrationDefinition>>()

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

    private fun getOrPutDatabase(databaseName: TypeName?): DatabaseObjectHolder? =
        databaseDefinitionMap.getOrPut(databaseName) { DatabaseObjectHolder() }

    override fun handle(processorManager: ProcessorManager, roundEnvironment: RoundEnvironment) {
        handlers.forEach { it.handle(processorManager, roundEnvironment) }

        val databaseDefinitions = getDatabaseHolderDefinitionList()
            .sortedBy { it.databaseDefinition?.outputClassName?.simpleName() }
        for (databaseHolderDefinition in databaseDefinitions) {
            try {

                if (databaseHolderDefinition.databaseDefinition == null) {
                    manager.logError(databaseHolderDefinition.getMissingDBRefs().joinToString("\n"))
                    continue
                }

                val manyToManyDefinitions = databaseHolderDefinition.manyToManyDefinitionMap.values

                val flattenedList =
                    manyToManyDefinitions.flatten().sortedBy { it.outputClassName?.simpleName() }
                for (manyToManyList in flattenedList) {
                    manyToManyList.prepareForWrite()
                    manyToManyList.writeBaseDefinition(processorManager)
                }

                // process all in next round.
                if (!manyToManyDefinitions.isEmpty()) {
                    manyToManyDefinitions.clear()
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

                val tableDefinitions = databaseHolderDefinition.tableDefinitionMap.values
                    .sortedBy { it.outputClassName?.simpleName() }

                tableDefinitions.forEach { it.writeBaseDefinition(processorManager) }

                val modelViewDefinitions = databaseHolderDefinition.modelViewDefinitionMap.values
                modelViewDefinitions
                    .sortedByDescending { it.priority }
                    .forEach { it.writeBaseDefinition(processorManager) }

                val queryModelDefinitions = databaseHolderDefinition.queryModelDefinitionMap.values
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
                    com.dbflow5.processor.ClassNames.FLOW_MANAGER_PACKAGE,
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
            databaseDefinitionMap.values.flatMap { it.tableDefinitionMap.values }
                .find { it.element == enclosingElement }

        // modelview check.
        if (find == null) {
            find = databaseDefinitionMap.values.flatMap { it.modelViewDefinitionMap.values }
                .find { it.element == enclosingElement }
        }
        // querymodel check
        if (find == null) {
            find = databaseDefinitionMap.values.flatMap { it.queryModelDefinitionMap.values }
                .find { it.element == enclosingElement }
        }
        return find != null
    }

}
