package com.raizlabs.dbflow5.processor

import com.raizlabs.dbflow5.processor.definition.BaseTableDefinition
import com.raizlabs.dbflow5.processor.definition.DatabaseDefinition
import com.raizlabs.dbflow5.processor.definition.DatabaseHolderDefinition
import com.raizlabs.dbflow5.processor.definition.DatabaseObjectHolder
import com.raizlabs.dbflow5.processor.definition.ManyToManyDefinition
import com.raizlabs.dbflow5.processor.definition.MigrationDefinition
import com.raizlabs.dbflow5.processor.definition.ModelViewDefinition
import com.raizlabs.dbflow5.processor.definition.QueryModelDefinition
import com.raizlabs.dbflow5.processor.definition.TableDefinition
import com.raizlabs.dbflow5.processor.definition.TypeConverterDefinition
import com.raizlabs.dbflow5.processor.definition.provider.ContentProviderDefinition
import com.raizlabs.dbflow5.processor.definition.provider.TableEndpointDefinition
import com.raizlabs.dbflow5.processor.utils.WriterUtils
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
class ProcessorManager internal constructor(val processingEnvironment: ProcessingEnvironment) : Handler {

    companion object {
        lateinit var manager: ProcessorManager
    }

    private val uniqueDatabases = arrayListOf<TypeName>()
    private val modelToDatabaseMap = hashMapOf<TypeName, TypeName>()
    val typeConverters = linkedMapOf<TypeName?, TypeConverterDefinition>()
    private val migrations = hashMapOf<TypeName?, MutableMap<Int, MutableList<MigrationDefinition>>>()

    private val databaseDefinitionMap = hashMapOf<TypeName?, DatabaseObjectHolder>()
    private val handlers = mutableSetOf<BaseContainerHandler<*>>()
    private val providerMap = hashMapOf<TypeName?, ContentProviderDefinition>()

    init {
        manager = this
    }

    fun addHandlers(vararg containerHandlers: BaseContainerHandler<*>) {
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

    fun addFlowManagerWriter(databaseDefinition: DatabaseDefinition) {
        val holderDefinition = getOrPutDatabase(databaseDefinition.elementClassName)
        holderDefinition?.databaseDefinition = databaseDefinition
    }

    fun getDatabaseHolderDefinitionList() = databaseDefinitionMap.values.toList()

    fun getDatabaseHolderDefinition(databaseName: TypeName?) = databaseDefinitionMap[databaseName]

    fun addTypeConverterDefinition(definition: TypeConverterDefinition) {
        typeConverters.put(definition.modelTypeName, definition)
    }

    fun getTypeConverterDefinition(typeName: TypeName?): TypeConverterDefinition? = typeConverters[typeName]

    fun addModelToDatabase(modelType: TypeName, databaseName: TypeName) {
        addDatabase(databaseName)
        modelToDatabaseMap.put(modelType, databaseName)
    }

    fun getDatabaseName(databaseTypeName: TypeName?) = getOrPutDatabase(databaseTypeName)?.databaseDefinition?.databaseClassName ?: ""

    fun addQueryModelDefinition(queryModelDefinition: QueryModelDefinition) {
        queryModelDefinition.elementClassName?.let {
            getOrPutDatabase(queryModelDefinition.databaseTypeName)?.
                queryModelDefinitionMap?.put(it, queryModelDefinition)
        }
    }

    fun addTableDefinition(tableDefinition: TableDefinition) {
        tableDefinition.elementClassName?.let {
            val holderDefinition = getOrPutDatabase(tableDefinition.databaseTypeName)
            holderDefinition?.tableDefinitionMap?.put(it, tableDefinition)
            holderDefinition?.tableNameMap?.let {
                if (holderDefinition.tableNameMap.containsKey(tableDefinition.tableName)) {
                    logError("Found duplicate table ${tableDefinition.tableName} " +
                        "for database ${holderDefinition.databaseDefinition?.databaseClassName}")
                } else tableDefinition.tableName?.let {
                    holderDefinition.tableNameMap.put(it, tableDefinition)
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

    fun getQueryModelDefinition(databaseName: TypeName?, typeName: TypeName?): QueryModelDefinition? {
        return getOrPutDatabase(databaseName)?.queryModelDefinitionMap?.get(typeName)
    }

    fun getModelViewDefinition(databaseName: TypeName?, typeName: TypeName?): ModelViewDefinition? {
        return getOrPutDatabase(databaseName)?.modelViewDefinitionMap?.get(typeName)
    }

    fun getReferenceDefinition(databaseName: TypeName?, typeName: TypeName?): BaseTableDefinition? {
        return getTableDefinition(databaseName, typeName)
            ?: getQueryModelDefinition(databaseName, typeName)
            ?: getModelViewDefinition(databaseName, typeName)
    }

    fun addModelViewDefinition(modelViewDefinition: ModelViewDefinition) {
        modelViewDefinition.elementClassName?.let {
            getOrPutDatabase(modelViewDefinition.databaseTypeName)?.
                modelViewDefinitionMap?.put(it, modelViewDefinition)
        }
    }

    fun getTypeConverters() = typeConverters.values.toHashSet().sortedBy { it.modelTypeName?.toString() }

    fun getTableDefinitions(databaseName: TypeName): List<TableDefinition> {
        val databaseHolderDefinition = getOrPutDatabase(databaseName)
        return (databaseHolderDefinition?.tableDefinitionMap?.values ?: arrayListOf())
            .toHashSet()
            .sortedBy { it.outputClassName?.simpleName() }
    }

    fun setTableDefinitions(tableDefinitionSet: MutableMap<TypeName, TableDefinition>, databaseName: TypeName) {
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

    fun setModelViewDefinitions(modelViewDefinitionMap: MutableMap<TypeName, ModelViewDefinition>, elementClassName: ClassName) {
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
        val migrationDefinitionMap = migrations.getOrPut(migrationDefinition.databaseName) { hashMapOf() }
        val migrationDefinitions = migrationDefinitionMap.getOrPut(migrationDefinition.version) { arrayListOf() }
        if (!migrationDefinitions.contains(migrationDefinition)) {
            migrationDefinitions.add(migrationDefinition)
        }
    }

    fun getMigrationsForDatabase(databaseName: TypeName) = migrations[databaseName] ?: hashMapOf<Int, List<MigrationDefinition>>()

    fun addContentProviderDefinition(contentProviderDefinition: ContentProviderDefinition) {
        contentProviderDefinition.elementTypeName?.let {
            val holderDefinition = getOrPutDatabase(contentProviderDefinition.databaseTypeName)
            holderDefinition?.providerMap?.put(it, contentProviderDefinition)
            providerMap.put(it, contentProviderDefinition)
        }
    }

    fun putTableEndpointForProvider(tableEndpointDefinition: TableEndpointDefinition) {
        val contentProviderDefinition = providerMap[tableEndpointDefinition.contentProviderName]
        if (contentProviderDefinition == null) {
            logError("Content Provider %1s was not found for the @TableEndpoint %1s",
                tableEndpointDefinition.contentProviderName, tableEndpointDefinition.elementClassName)
        } else {
            contentProviderDefinition.endpointDefinitions.add(tableEndpointDefinition)
        }
    }

    fun logError(callingClass: KClass<*>?, error: String?, vararg args: Any?) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format("*==========*${callingClass ?: ""} :" + error?.trim() + "*==========*", *args))
        var stackTraceElements = Thread.currentThread().stackTrace
        if (stackTraceElements.size > 8) {
            stackTraceElements = stackTraceElements.copyOf(8)
        }
        stackTraceElements.forEach { messager.printMessage(Diagnostic.Kind.ERROR, it.toString()) }
    }

    fun logError(error: String?, vararg args: Any?) = logError(callingClass = null, error = error, args = *args)

    fun logWarning(error: String?, vararg args: Any) {
        messager.printMessage(Diagnostic.Kind.WARNING, String.format("*==========*\n$error\n*==========*", *args))
    }

    fun logWarning(callingClass: Class<*>, error: String, vararg args: Any) {
        logWarning("$callingClass : $error", *args)
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

                val flattenedList = manyToManyDefinitions.flatten().sortedBy { it.outputClassName?.simpleName() }
                for (manyToManyList in flattenedList) {
                    manyToManyList.prepareForWrite()
                    WriterUtils.writeBaseDefinition(manyToManyList, processorManager)
                }

                // process all in next round.
                if (!manyToManyDefinitions.isEmpty()) {
                    manyToManyDefinitions.clear()
                    continue
                }

                if (roundEnvironment.processingOver()) {
                    val validator = ContentProviderValidator()
                    val contentProviderDefinitions = databaseHolderDefinition.providerMap.values
                        .sortedBy { it.outputClassName?.simpleName() }
                    contentProviderDefinitions.forEach { contentProviderDefinition ->
                        contentProviderDefinition.prepareForWrite()
                        if (validator.validate(processorManager, contentProviderDefinition)) {
                            WriterUtils.writeBaseDefinition(contentProviderDefinition, processorManager)
                        }
                    }
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

                tableDefinitions.forEach { WriterUtils.writeBaseDefinition(it, processorManager) }

                val modelViewDefinitions = databaseHolderDefinition.modelViewDefinitionMap.values
                modelViewDefinitions
                    .sortedByDescending { it.priority }
                    .forEach { WriterUtils.writeBaseDefinition(it, processorManager) }

                val queryModelDefinitions = databaseHolderDefinition.queryModelDefinitionMap.values
                    .sortedBy { it.outputClassName?.simpleName() }
                queryModelDefinitions.forEach { WriterUtils.writeBaseDefinition(it, processorManager) }

                tableDefinitions.forEach {
                    try {
                        it.writePackageHelper(processorManager.processingEnvironment)
                    } catch (e: FilerException) { /*Ignored intentionally to allow multi-round table generation*/
                    }
                }

                modelViewDefinitions.forEach {
                    try {
                        it.writePackageHelper(processorManager.processingEnvironment)
                    } catch (e: FilerException) { /*Ignored intentionally to allow multi-round table generation*/
                    }
                }

                queryModelDefinitions.forEach {
                    try {
                        it.writePackageHelper(processorManager.processingEnvironment)
                    } catch (e: FilerException) { /*Ignored intentionally to allow multi-round table generation*/
                    }
                }
            } catch (e: IOException) {
            }

        }

        try {
            val databaseHolderDefinition = DatabaseHolderDefinition(processorManager)
            if (!databaseHolderDefinition.isGarbage()) {
                JavaFile.builder(ClassNames.FLOW_MANAGER_PACKAGE,
                    databaseHolderDefinition.typeSpec).build()
                    .writeTo(processorManager.processingEnvironment.filer)
            }
        } catch (e: FilerException) {
        } catch (e: IOException) {
            logError(e.message)
        }
    }

    fun elementBelongsInTable(element: Element): Boolean {
        val enclosingElement = element.enclosingElement
        var find: BaseTableDefinition? = databaseDefinitionMap.values.flatMap { it.tableDefinitionMap.values }
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
