package com.dbflow5.processor.definition

import com.dbflow5.processor.ProcessorManager
import com.squareup.javapoet.TypeName

interface DatabaseObjectHolder {

    var databaseDefinition: DatabaseDefinition?

    fun putTable(tableDefinition: TableDefinition, processorManager: ProcessorManager)
    fun setTableDefinitions(
        map: MutableMap<TypeName, TableDefinition>
    )

    fun table(typeName: TypeName?): TableDefinition?
    val tables: List<TableDefinition>

    fun putQueryModel(
        queryModelDefinition: QueryModelDefinition,
        processorManager: ProcessorManager
    )

    fun queryModel(typeName: TypeName?): QueryModelDefinition?
    val queryModels: List<QueryModelDefinition>

    fun putModelView(modelViewDefinition: ModelViewDefinition, processorManager: ProcessorManager)
    fun setModelViewDefinitions(
        modelViewDefinitionMap: MutableMap<TypeName, ModelViewDefinition>
    )

    fun modelView(typeName: TypeName?): ModelViewDefinition?
    val modelViews: List<ModelViewDefinition>

    fun putManyToMany(
        manyToManyDefinition: ManyToManyDefinition,
        processorManager: ProcessorManager
    )

    val manyToManys: List<ManyToManyDefinition>
    fun clearManyToMany()

    /**
     * Retrieve what database class they're trying to reference.
     */
    fun getMissingDBRefs(): List<String> {
        if (databaseDefinition == null) {
            val list = mutableListOf<String>()
            tables.forEach {
                list += "Database ${it.associationalBehavior.databaseTypeName} not found for Table ${it.associationalBehavior.name}"
            }
            queryModels.forEach {
                list += "Database ${it.associationalBehavior.databaseTypeName} not found for QueryModel ${it.elementName}"
            }
            modelViews.forEach {
                list += "Database ${it.associationalBehavior.databaseTypeName} not found for ModelView ${it.elementName}"
            }
            return list

        } else {
            return listOf()
        }
    }
}

/**
 * Description: Provides overarching holder for [DatabaseDefinition], [TableDefinition],
 * and more. So we can safely use.
 */
open class UniqueDatabaseObjectHolder : DatabaseObjectHolder {

    override var databaseDefinition: DatabaseDefinition? = null
        set(databaseDefinition) {
            field = databaseDefinition
            field?.objectHolder = this
        }

    private var tableDefinitionMap: MutableMap<TypeName, TableDefinition> = hashMapOf()
    private val tableNameMap: MutableMap<String, TableDefinition> = hashMapOf()

    private val queryModelDefinitionMap: MutableMap<TypeName, QueryModelDefinition> = hashMapOf()
    private var modelViewDefinitionMap: MutableMap<TypeName, ModelViewDefinition> = hashMapOf()
    private val manyToManyDefinitionMap: MutableMap<TypeName, MutableList<ManyToManyDefinition>> =
        hashMapOf()

    protected fun associateTable(
        tableDefinition: TableDefinition,
        fn: () -> Unit,
    ) {
        tableDefinition.elementClassName?.let { elementName ->
            tableDefinitionMap[elementName] = tableDefinition
            reassociateIfDefinition(tableDefinition)
            fn()
        }
    }

    override fun putTable(
        tableDefinition: TableDefinition,
        processorManager: ProcessorManager
    ) {
        associateTable(tableDefinition) {
            tableNameMap.let {
                val tableName = tableDefinition.associationalBehavior.name
                if (tableNameMap.containsKey(tableName)
                    && (
                        tableDefinition.elementClassName == null ||
                            !tableDefinitionMap.containsKey(tableDefinition.elementClassName as TypeName)
                        )
                ) {
                    processorManager.logError(
                        "Found duplicate table $tableName " +
                            "for database ${databaseDefinition?.elementName}"
                    )
                } else {
                    tableNameMap[tableName] = tableDefinition
                }
            }
        }
    }

    override fun setTableDefinitions(map: MutableMap<TypeName, TableDefinition>) {
        this.tableDefinitionMap = map
    }

    override fun table(typeName: TypeName?) = tableDefinitionMap[typeName]
    override val tables: List<TableDefinition>
        get() = tableDefinitionMap.values
            .toHashSet()
            .sortedBy { it.outputClassName?.simpleName() }

    override fun putQueryModel(
        queryModelDefinition: QueryModelDefinition,
        processorManager: ProcessorManager
    ) {
        queryModelDefinition.elementClassName?.let {
            queryModelDefinitionMap.put(it, queryModelDefinition)
            reassociateIfDefinition(queryModelDefinition)
        }
    }

    override fun queryModel(typeName: TypeName?) = queryModelDefinitionMap[typeName]
    override val queryModels: List<QueryModelDefinition>
        get() = queryModelDefinitionMap
            .values
            .toHashSet()
            .sortedBy { it.outputClassName?.simpleName() }

    override fun putModelView(
        modelViewDefinition: ModelViewDefinition,
        processorManager: ProcessorManager
    ) {
        modelViewDefinition.elementClassName?.let {
            modelViewDefinitionMap.put(it, modelViewDefinition)
            reassociateIfDefinition(modelViewDefinition)
        }
    }

    override fun modelView(typeName: TypeName?) = modelViewDefinitionMap[typeName]

    override val modelViews: List<ModelViewDefinition>
        get() = modelViewDefinitionMap.values
            .toHashSet()
            .sortedBy { it.outputClassName?.simpleName() }
            .sortedByDescending { it.priority }

    override fun setModelViewDefinitions(
        modelViewDefinitionMap: MutableMap<TypeName, ModelViewDefinition>
    ) {
        this.modelViewDefinitionMap = modelViewDefinitionMap
    }

    override fun putManyToMany(
        manyToManyDefinition: ManyToManyDefinition,
        processorManager: ProcessorManager
    ) {
        manyToManyDefinition.elementClassName?.let { elementClassName ->
            manyToManyDefinitionMap.getOrPut(elementClassName) { arrayListOf() }
                .add(manyToManyDefinition)
        }
    }

    override val manyToManys: List<ManyToManyDefinition> = manyToManyDefinitionMap.values.flatten()

    override fun clearManyToMany() {
        manyToManyDefinitionMap.values.clear()
    }

    private fun reassociateIfDefinition(entityDefinition: EntityDefinition) {
        if (databaseDefinition != null) {
            entityDefinition.declaredDefinition = databaseDefinition
        }
    }
}

class DeclarativeDatabaseObjectHolder : UniqueDatabaseObjectHolder() {

    override fun putTable(tableDefinition: TableDefinition, processorManager: ProcessorManager) {
        // skips unique check because may belong to multiple dbs.
        associateTable(tableDefinition) {}
    }
}
