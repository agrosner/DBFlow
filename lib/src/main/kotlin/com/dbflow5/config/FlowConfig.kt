package com.dbflow5.config

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

    /**
     * Merges two [FlowConfig] together by combining an existing config. Any new specified [DatabaseConfig]
     * will override existing ones.
     */
    internal fun merge(flowConfig: FlowConfig): FlowConfig = FlowConfig(
        context = flowConfig.context,
        databaseConfigMap = databaseConfigMap.entries
            .map { (key, value) ->
                key to (flowConfig.databaseConfigMap[key] ?: value)
            }.toMap(),
        databaseHolders = databaseHolders.plus(flowConfig.databaseHolders),
        openDatabasesOnInit = flowConfig.openDatabasesOnInit)

    class Builder(context: Context) {

        internal val context: Context = context.applicationContext
        internal var databaseHolders: MutableSet<Class<out DatabaseHolder>> = hashSetOf()
        internal val databaseConfigMap: MutableMap<Class<*>, DatabaseConfig> = hashMapOf()
        internal var openDatabasesOnInit: Boolean = false

        fun addDatabaseHolder(databaseHolderClass: Class<out DatabaseHolder>) = apply {
            databaseHolders.add(databaseHolderClass)
        }

        inline fun <reified T : DatabaseHolder> databaseHolder() = addDatabaseHolder(T::class.java)

        fun database(databaseConfig: DatabaseConfig) = apply {
            databaseConfigMap.put(databaseConfig.databaseClass, databaseConfig)
        }

        inline fun <reified T : Any> database(
            fn: DatabaseConfig.Builder.() -> Unit = {},
            openHelperCreator: OpenHelperCreator? = null,
        ) = database(DatabaseConfig.builder(T::class, openHelperCreator).apply(fn).build())

        inline fun <reified T : Any> inMemoryDatabase(
            fn: DatabaseConfig.Builder.() -> Unit = {},
            openHelperCreator: OpenHelperCreator? = null,
        ) = database<T>({
            inMemory()
            fn()
        }, openHelperCreator)

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

inline fun flowConfig(context: Context, fn: FlowConfig.Builder.() -> Unit): FlowConfig =
    FlowConfig.builder(context).apply(fn).build()