package com.raizlabs.dbflow5.config

import kotlin.reflect.KClass


expect class FlowConfig : InternalFlowConfig {

    class Builder : InternalFlowConfig.InternalBuilder
}

/**
 * Description: The main configuration instance for DBFlow. This
 */
abstract class InternalFlowConfig(val databaseHolders: Set<KClass<out DatabaseHolder>> = setOf(),
                                  val databaseConfigMap: Map<KClass<*>, DatabaseConfig> = mapOf(),
                                  val openDatabasesOnInit: Boolean = false) {

    protected constructor(builder: InternalBuilder) : this(
        databaseHolders = builder.databaseHolders.toSet(),
        databaseConfigMap = builder.databaseConfigMap,
        openDatabasesOnInit = builder.openDatabasesOnInit
    )

    fun getConfigForDatabase(databaseClass: KClass<*>): DatabaseConfig? {
        return databaseConfigMap[databaseClass]
    }

    /**
     * Merges two [FlowConfig] together by combining an existing config. Any new specified [DatabaseConfig]
     * will override existing ones.
     */
    abstract fun merge(flowConfig: FlowConfig): FlowConfig

    abstract class InternalBuilder {

        var databaseHolders: MutableSet<KClass<out DatabaseHolder>> = hashSetOf()
        val databaseConfigMap: MutableMap<KClass<*>, DatabaseConfig> = hashMapOf()
        var openDatabasesOnInit: Boolean = false

        fun addDatabaseHolder(databaseHolderClass: KClass<out DatabaseHolder>) = apply {
            databaseHolders.add(databaseHolderClass)
        }

        fun database(databaseConfig: DatabaseConfig) = apply {
            databaseConfigMap.put(databaseConfig.databaseClass, databaseConfig)
        }

        /**
         * @param openDatabasesOnInit true if we want all databases open.
         * @return True to open all associated databases in DBFlow on calling of [FlowManager.init]
         */
        fun openDatabasesOnInit(openDatabasesOnInit: Boolean) = apply {
            this.openDatabasesOnInit = openDatabasesOnInit
        }

        abstract fun build(): FlowConfig
    }
}
