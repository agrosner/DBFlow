package com.raizlabs.android.dbflow.processor.model

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.google.common.collect.Sets
import com.raizlabs.android.dbflow.processor.ClassNames
import com.raizlabs.android.dbflow.processor.definition.*
import com.raizlabs.android.dbflow.processor.definition.method.DatabaseDefinition
import com.raizlabs.android.dbflow.processor.definition.method.DatabaseHolderDefinition
import com.raizlabs.android.dbflow.processor.handler.BaseContainerHandler
import com.raizlabs.android.dbflow.processor.handler.Handler
import com.raizlabs.android.dbflow.processor.utils.WriterUtils
import com.raizlabs.android.dbflow.processor.validator.ContentProviderValidator
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeName
import java.io.IOException
import java.util.*
import javax.annotation.processing.FilerException
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic
import kotlin.reflect.KClass

/**
 * Description: Holds onto [com.raizlabs.android.dbflow.processor.definition.Definition], Writers,
 * and provides some handy methods for interacting with the [javax.annotation.processing.Processor]
 */
class ProcessorManager(val processingEnvironment: ProcessingEnvironment) : Handler {

    companion object {

        lateinit var manager: ProcessorManager
    }

    private val uniqueDatabases = Lists.newArrayList<TypeName>()
    private val modelToDatabaseMap = Maps.newHashMap<TypeName, TypeName>()
    private val typeConverters = Maps.newHashMap<TypeName, TypeConverterDefinition>()
    private val migrations = Maps.newHashMap<TypeName, MutableMap<Int, MutableList<MigrationDefinition>>>()

    private val databaseDefinitionMap = Maps.newHashMap<TypeName, DatabaseHolderDefinition>()
    private val handlers = ArrayList<BaseContainerHandler<*>>()
    private val providerMap = Maps.newHashMap<TypeName, ContentProviderDefinition>()


    init {
        manager = this
    }

    fun addHandlers(vararg containerHandlers: BaseContainerHandler<*>) {
        for (containerHandler in containerHandlers) {
            if (!handlers.contains(containerHandler)) {
                handlers.add(containerHandler)
            }
        }
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

    fun getDatabaseHolderDefinitionMap() = ArrayList(databaseDefinitionMap.values)

    fun getDatabaseHolderDefinition(databaseName: TypeName?) = databaseDefinitionMap[databaseName]

    fun addTypeConverterDefinition(definition: TypeConverterDefinition) {
        typeConverters.put(definition.modelTypeName, definition)
    }

    fun getTypeConverterDefinition(typeName: TypeName?) = typeConverters[typeName]

    fun addModelToDatabase(modelType: TypeName, databaseName: TypeName) {
        addDatabase(databaseName)
        modelToDatabaseMap.put(modelType, databaseName)
    }

    fun getDatabaseName(databaseTypeName: TypeName?) = getOrPutDatabase(databaseTypeName)?.databaseDefinition?.databaseName ?: ""

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
                    logError("Found duplicate table %1s for database %1s", tableDefinition.tableName,
                            holderDefinition.databaseDefinition?.databaseName)
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
                var manyToManyDefinitions: MutableList<ManyToManyDefinition>? = it[elementClassName]
                if (manyToManyDefinitions == null) {
                    manyToManyDefinitions = ArrayList<ManyToManyDefinition>()
                    it.put(elementClassName, manyToManyDefinitions)
                }

                manyToManyDefinitions.add(manyToManyDefinition)
            }
        }
    }

    fun getTableDefinition(databaseName: TypeName?, typeName: TypeName?): TableDefinition? {
        return getOrPutDatabase(databaseName)?.tableDefinitionMap?.get(typeName)
    }

    fun addModelViewDefinition(modelViewDefinition: ModelViewDefinition) {
        modelViewDefinition.elementClassName?.let {
            getOrPutDatabase(modelViewDefinition.databaseName)?.
                    modelViewDefinitionMap?.put(it, modelViewDefinition)
        }
    }

    fun getTypeConverters(): Set<TypeConverterDefinition> {
        return Sets.newHashSet(typeConverters.values)
    }

    fun getTableDefinitions(databaseName: TypeName): Set<TableDefinition> {
        val databaseHolderDefinition = getOrPutDatabase(databaseName)
        return Sets.newHashSet(databaseHolderDefinition?.tableDefinitionMap?.values ?: arrayListOf())
    }

    fun setTableDefinitions(tableDefinitionSet: MutableMap<TypeName, TableDefinition>, databaseName: TypeName) {
        val databaseDefinition = getOrPutDatabase(databaseName)
        databaseDefinition?.tableDefinitionMap = tableDefinitionSet
    }

    fun getModelViewDefinitions(databaseName: TypeName): Set<ModelViewDefinition> {
        val databaseDefinition = getOrPutDatabase(databaseName)
        return Sets.newHashSet(databaseDefinition?.modelViewDefinitionMap?.values ?: arrayListOf())
    }

    fun setModelViewDefinitions(modelViewDefinitionMap: MutableMap<TypeName, ModelViewDefinition>, elementClassName: ClassName) {
        val databaseDefinition = getOrPutDatabase(elementClassName)
        databaseDefinition?.modelViewDefinitionMap = modelViewDefinitionMap
    }

    fun getQueryModelDefinitions(databaseName: TypeName): Set<QueryModelDefinition> {
        val databaseDefinition = getOrPutDatabase(databaseName)
        return Sets.newHashSet(databaseDefinition?.queryModelDefinitionMap?.values ?: arrayListOf())
    }

    fun addMigrationDefinition(migrationDefinition: MigrationDefinition) {
        var migrationDefinitionMap: MutableMap<Int, MutableList<MigrationDefinition>>? = migrations[migrationDefinition.databaseName]
        if (migrationDefinitionMap == null) {
            migrationDefinitionMap = Maps.newHashMap<Int, MutableList<MigrationDefinition>>()
            migrations.put(migrationDefinition.databaseName, migrationDefinitionMap)
        }

        var migrationDefinitions: MutableList<MigrationDefinition>? = migrationDefinitionMap!![migrationDefinition.version]
        if (migrationDefinitions == null) {
            migrationDefinitions = Lists.newArrayList<MigrationDefinition>()
            migrationDefinitionMap.put(migrationDefinition.version, migrationDefinitions)
        }

        if (!migrationDefinitions!!.contains(migrationDefinition)) {
            migrationDefinitions.add(migrationDefinition)
        }
    }

    fun getMigrationsForDatabase(databaseName: TypeName): Map<Int, List<MigrationDefinition>> {
        val migrationDefinitions = migrations[databaseName]
        if (migrationDefinitions != null) {
            return migrationDefinitions
        } else {
            return Maps.newHashMap<Int, List<MigrationDefinition>>()
        }
    }

    fun addContentProviderDefinition(contentProviderDefinition: ContentProviderDefinition) {
        contentProviderDefinition.elementTypeName?.let {
            val holderDefinition = getOrPutDatabase(contentProviderDefinition.databaseName)
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
        messager.printMessage(Diagnostic.Kind.ERROR, String.format("*==========*$callingClass :" + error?.trim { it <= ' ' } + "*==========*", *args))
        var stackTraceElements = Thread.currentThread().stackTrace
        if (stackTraceElements.size > 5) {
            stackTraceElements = Arrays.copyOf(stackTraceElements, 5)
        }
        for (stackTrace in stackTraceElements) {
            messager.printMessage(Diagnostic.Kind.ERROR, stackTrace.toString())
        }
    }

    fun logError(error: String?, vararg args: Any?) = logError(callingClass = null, error = error, args = args)

    fun logWarning(error: String, vararg args: Any) {
        messager.printMessage(Diagnostic.Kind.WARNING, String.format("*==========*\n$error\n*==========*", *args))
    }

    fun logWarning(callingClass: Class<*>, error: String, vararg args: Any) {
        logWarning("$callingClass : $error", *args)
    }

    private fun getOrPutDatabase(databaseName: TypeName?): DatabaseHolderDefinition? {
        var holderDefinition: DatabaseHolderDefinition? = databaseDefinitionMap[databaseName]
        if (holderDefinition == null) {
            holderDefinition = DatabaseHolderDefinition()
            databaseDefinitionMap.put(databaseName, holderDefinition)
        }
        return holderDefinition
    }

    override fun handle(processorManager: ProcessorManager, roundEnvironment: RoundEnvironment) {
        for (containerHandler in handlers) {
            containerHandler.handle(processorManager, roundEnvironment)
        }

        val databaseDefinitions = getDatabaseHolderDefinitionMap()
        for (databaseHolderDefinition in databaseDefinitions) {
            try {

                if (databaseHolderDefinition.databaseDefinition == null) {
                    ProcessorManager.manager.logError("Found null db with: %1s tables, %1s modelviews. " + "Attempt to rebuild project should fix this intermittant issue.",
                            databaseHolderDefinition.tableNameMap.values.size,
                            databaseHolderDefinition.modelViewDefinitionMap.values.size)
                    ProcessorManager.manager.logError("Found tables: " + databaseHolderDefinition.tableNameMap.values)
                    continue
                }

                val manyToManyDefinitions = databaseHolderDefinition.manyToManyDefinitionMap.values
                for (manyToManyList in manyToManyDefinitions) {
                    for (manyToMany in manyToManyList) {
                        manyToMany.prepareForWrite()
                        WriterUtils.writeBaseDefinition(manyToMany, processorManager)
                    }
                }

                // process all in next round.
                if (!manyToManyDefinitions.isEmpty()) {
                    manyToManyDefinitions.clear()
                    continue
                }

                val validator = ContentProviderValidator()
                val contentProviderDefinitions = databaseHolderDefinition.providerMap.values
                for (contentProviderDefinition in contentProviderDefinitions) {
                    contentProviderDefinition.prepareForWrite()
                    if (validator.validate(processorManager, contentProviderDefinition)) {
                        WriterUtils.writeBaseDefinition(contentProviderDefinition, processorManager)
                    }
                }

                databaseHolderDefinition.databaseDefinition?.validateAndPrepareToWrite()

                if (roundEnvironment.processingOver()) {
                    databaseHolderDefinition.databaseDefinition?.let {
                        JavaFile.builder(it.packageName, it.typeSpec).build()
                                .writeTo(processorManager.processingEnvironment.filer)
                    }
                }


                val tableDefinitions = databaseHolderDefinition.tableDefinitionMap.values

                for (tableDefinition in tableDefinitions) {
                    WriterUtils.writeBaseDefinition(tableDefinition, processorManager)
                }

                val modelViewDefinitions = ArrayList(databaseHolderDefinition.modelViewDefinitionMap.values)
                Collections.sort(modelViewDefinitions)
                for (modelViewDefinition in modelViewDefinitions) {
                    WriterUtils.writeBaseDefinition(modelViewDefinition, processorManager)
                }

                val queryModelDefinitions = databaseHolderDefinition.queryModelDefinitionMap.values
                for (queryModelDefinition in queryModelDefinitions) {
                    WriterUtils.writeBaseDefinition(queryModelDefinition, processorManager)
                }

                for (tableDefinition in tableDefinitions) {
                    try {
                        tableDefinition.writePackageHelper(processorManager.processingEnvironment)
                    } catch (e: FilerException) { /*Ignored intentionally to allow multi-round table generation*/
                    }

                }

                for (modelViewDefinition in modelViewDefinitions) {
                    try {
                        modelViewDefinition.writePackageHelper(processorManager.processingEnvironment)
                    } catch (e: FilerException) { /*Ignored intentionally to allow multi-round table generation*/
                    }

                }

                for (queryModelDefinition in queryModelDefinitions) {
                    try {
                        queryModelDefinition.writePackageHelper(processorManager.processingEnvironment)
                    } catch (e: FilerException) { /*Ignored intentionally to allow multi-round table generation*/
                    }

                }
            } catch (e: IOException) {
            }

        }

        if (roundEnvironment.processingOver()) {
            try {
                JavaFile.builder(ClassNames.FLOW_MANAGER_PACKAGE,
                        FlowManagerHolderDefinition(processorManager).typeSpec).build().writeTo(processorManager.processingEnvironment.filer)
            } catch (e: IOException) {
                logError(e.message)
            }

        }
    }

}
