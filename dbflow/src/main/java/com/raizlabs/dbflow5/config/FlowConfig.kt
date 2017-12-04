package com.raizlabs.dbflow5.config

import android.content.Context

/**
 * Description: The main configuration instance for DBFlow. This
 */
class FlowConfig(val context: Context,
                 val databaseHolders: Set<Class<out DatabaseHolder>> = setOf(),
                 val databaseConfigMap: Map<Class<*>, DatabaseConfig> = mapOf(),
                 val openDatabasesOnInit: Boolean = false) {

    internal constructor(builder: Builder) : this(
        databaseHolders = builder.databaseHolders.toSet(),
        databaseConfigMap = builder.databaseConfigMap,
        context = builder.context,
        openDatabasesOnInit = builder.openDatabasesOnInit
    )

    fun getConfigForDatabase(databaseClass: Class<*>): DatabaseConfig? {
        return databaseConfigMap[databaseClass]
    }

    class Builder(context: Context) {

        internal val context: Context = context.applicationContext
        internal var databaseHolders: MutableSet<Class<out DatabaseHolder>> = hashSetOf()
        internal val databaseConfigMap: MutableMap<Class<*>, DatabaseConfig> = hashMapOf()
        internal var openDatabasesOnInit: Boolean = false

        fun addDatabaseHolder(databaseHolderClass: Class<out DatabaseHolder>) = apply {
            databaseHolders.add(databaseHolderClass)
        }

        fun addDatabaseConfig(databaseConfig: DatabaseConfig) = apply {
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

        fun builder(context: Context): Builder = Builder(context)
    }
}
