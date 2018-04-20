package com.raizlabs.dbflow5.config

import com.raizlabs.dbflow5.JvmStatic
import com.raizlabs.dbflow5.database.DBFlowDatabase
import com.raizlabs.dbflow5.database.DatabaseCallback
import com.raizlabs.dbflow5.database.OpenHelper
import com.raizlabs.dbflow5.runtime.ModelNotifier
import com.raizlabs.dbflow5.transaction.BaseTransactionManager
import kotlin.reflect.KClass

typealias OpenHelperCreator = (DBFlowDatabase, DatabaseCallback?) -> OpenHelper
typealias TransactionManagerCreator = (DBFlowDatabase) -> BaseTransactionManager

expect class DatabaseConfig : InternalDatabaseConfig {

    class Builder(databaseClass: KClass<*>,
                  openHelperCreator: OpenHelperCreator?) : InternalBuilder

    companion object {

        @JvmStatic
        fun builder(database: KClass<*>, openHelperCreator: OpenHelperCreator): DatabaseConfig.Builder

        @JvmStatic
        fun inMemoryBuilder(database: KClass<*>, openHelperCreator: OpenHelperCreator): DatabaseConfig.Builder
    }
}

/**
 * Description:
 */
abstract class InternalDatabaseConfig(
    val databaseClass: KClass<*>,
    val openHelperCreator: OpenHelperCreator? = null,
    val transactionManagerCreator: TransactionManagerCreator? = null,
    val callback: DatabaseCallback? = null,
    val tableConfigMap: Map<KClass<*>, TableConfig<*>> = mapOf(),
    val modelNotifier: ModelNotifier? = null,
    val isInMemory: Boolean = false,
    val databaseName: String? = null,
    val databaseExtensionName: String? = null) {

    protected constructor(builder: InternalDatabaseConfig.InternalBuilder) : this(
        // convert java interface to kotlin function.
        openHelperCreator = builder.openHelperCreator,
        databaseClass = builder.databaseClass,
        transactionManagerCreator = builder.transactionManagerCreator,
        callback = builder.callback,
        tableConfigMap = builder.tableConfigMap,
        modelNotifier = builder.modelNotifier,
        isInMemory = builder.inMemory,
        databaseName = builder.databaseName
            ?: throw IllegalArgumentException("Missing Database name in builder for ${builder.databaseClass}"),
        databaseExtensionName = when {
            builder.databaseExtensionName == null -> ".db"
            builder.databaseExtensionName.isNullOrEmpty().not() -> ".${builder.databaseExtensionName}"
            else -> ""
        })

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getTableConfigForTable(modelClass: KClass<T>): TableConfig<T>? =
        tableConfigMap[modelClass] as TableConfig<T>?

    /**
     * Build compatibility class for Java. Use the [DatabaseConfig] class directly if Kotlin consumer.
     */
    abstract class InternalBuilder(internal val databaseClass: KClass<*>,
                                   internal val openHelperCreator: OpenHelperCreator? = null) {

        internal var transactionManagerCreator: TransactionManagerCreator? = null
        internal var callback: DatabaseCallback? = null
        internal val tableConfigMap: MutableMap<KClass<*>, TableConfig<*>> = hashMapOf()
        internal var modelNotifier: ModelNotifier? = null
        internal var inMemory = false
        internal var databaseName: String? = null
        internal var databaseExtensionName: String? = null

        fun transactionManagerCreator(creator: TransactionManagerCreator) = applyBuilder {
            this.transactionManagerCreator = creator
        }

        fun helperListener(callback: DatabaseCallback) = applyBuilder {
            this.callback = callback
        }

        fun addTableConfig(tableConfig: TableConfig<*>) = applyBuilder {
            tableConfigMap[tableConfig.tableClass] = tableConfig
        }

        fun modelNotifier(modelNotifier: ModelNotifier) = applyBuilder {
            this.modelNotifier = modelNotifier
        }

        fun inMemory() = applyBuilder {
            inMemory = true
        }

        /**
         * @return Pass in dynamic database name here. Otherwise it defaults to class name.
         */
        fun databaseName(name: String) = applyBuilder {
            databaseName = name
        }

        /**
         * @return Pass in the extension for the DB here.
         * Otherwise defaults to ".db". If empty string passed, no extension is used.
         */
        fun extensionName(name: String) = applyBuilder {
            databaseExtensionName = name
        }

        abstract fun build(): DatabaseConfig

        private fun applyBuilder(fn: InternalBuilder.() -> Unit): DatabaseConfig.Builder = apply(fn) as DatabaseConfig.Builder
    }

    companion object {

        @JvmStatic
        fun builder(database: KClass<*>, openHelperCreator: OpenHelperCreator): DatabaseConfig.Builder =
            DatabaseConfig.Builder(database, openHelperCreator)

        @JvmStatic
        fun inMemoryBuilder(database: KClass<*>, openHelperCreator: OpenHelperCreator): DatabaseConfig.Builder =
            DatabaseConfig.Builder(database, openHelperCreator).inMemory()
    }
}
