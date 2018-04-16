package com.raizlabs.dbflow5.config

import com.raizlabs.dbflow5.KClass

/**
 * Description: The main configuration instance for DBFlow. This
 */
class FlowConfig(val databaseHolders: Set<KClass<out DatabaseHolder>> = setOf(),
                 val databaseConfigMap: Map<KClass<*>, DatabaseConfig> = mapOf(),
                 val openDatabasesOnInit: Boolean = false) {

    internal constructor(builder: Builder) : this(
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
    internal fun merge(flowConfig: FlowConfig): FlowConfig = FlowConfig(
        databaseConfigMap = databaseConfigMap.entries
            .map { (key, value) ->
                key to (flowConfig.databaseConfigMap[key] ?: value)
            }.toMap(),
        databaseHolders = databaseHolders.plus(flowConfig.databaseHolders),
        openDatabasesOnInit = flowConfig.openDatabasesOnInit)

    class Builder {

        internal var databaseHolders: MutableSet<KClass<out DatabaseHolder>> = hashSetOf()
        internal val databaseConfigMap: MutableMap<KClass<*>, DatabaseConfig> = hashMapOf()
        internal var openDatabasesOnInit: Boolean = false

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

        fun build() = FlowConfig(this)
    }

    companion object {

        fun builder(): Builder = Builder()
    }
}
