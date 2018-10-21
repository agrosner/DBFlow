package com.dbflow5.config

import com.dbflow5.database.DatabaseCallback
import com.dbflow5.database.OpenHelper
import com.dbflow5.isNotNullOrEmpty
import com.dbflow5.runtime.ModelNotifier
import com.dbflow5.transaction.BaseTransactionManager
import kotlin.reflect.KClass

typealias OpenHelperCreator = (DBFlowDatabase, DatabaseCallback?) -> OpenHelper
typealias TransactionManagerCreator = (DBFlowDatabase) -> BaseTransactionManager

/**
 * Description:
 */
class DatabaseConfig(
    val databaseClass: Class<*>,
    val openHelperCreator: OpenHelperCreator? = null,
    val transactionManagerCreator: TransactionManagerCreator? = null,
    val callback: DatabaseCallback? = null,
    val tableConfigMap: Map<Class<*>, TableConfig<*>> = mapOf(),
    val modelNotifier: ModelNotifier? = null,
    val isInMemory: Boolean = false,
    val databaseName: String? = null,
    val databaseExtensionName: String? = null) {

    internal constructor(builder: Builder) : this(
        // convert java interface to kotlin function.
        openHelperCreator = builder.openHelperCreator,
        databaseClass = builder.databaseClass,
        transactionManagerCreator = builder.transactionManagerCreator,
        callback = builder.callback,
        tableConfigMap = builder.tableConfigMap,
        modelNotifier = builder.modelNotifier,
        isInMemory = builder.inMemory,
        databaseName = builder.databaseName ?: builder.databaseClass.simpleName,
        databaseExtensionName = when {
            builder.databaseExtensionName == null -> ".db"
            builder.databaseExtensionName.isNotNullOrEmpty() -> ".${builder.databaseExtensionName}"
            else -> ""
        })

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getTableConfigForTable(modelClass: Class<T>): TableConfig<T>? =
        tableConfigMap[modelClass] as TableConfig<T>?

    /**
     * Build compatibility class for Java. Use the [DatabaseConfig] class directly if Kotlin consumer.
     */
    class Builder(internal val databaseClass: Class<*>,
                  internal val openHelperCreator: OpenHelperCreator? = null) {

        internal var transactionManagerCreator: TransactionManagerCreator? = null
        internal var callback: DatabaseCallback? = null
        internal val tableConfigMap: MutableMap<Class<*>, TableConfig<*>> = hashMapOf()
        internal var modelNotifier: ModelNotifier? = null
        internal var inMemory = false
        internal var databaseName: String? = null
        internal var databaseExtensionName: String? = null

        constructor(kClass: KClass<*>, openHelperCreator: OpenHelperCreator)
            : this(kClass.java, openHelperCreator)

        fun transactionManagerCreator(creator: TransactionManagerCreator) = apply {
            this.transactionManagerCreator = creator
        }

        fun helperListener(callback: DatabaseCallback) = apply {
            this.callback = callback
        }

        fun addTableConfig(tableConfig: TableConfig<*>) = apply {
            tableConfigMap.put(tableConfig.tableClass, tableConfig)
        }

        fun modelNotifier(modelNotifier: ModelNotifier) = apply {
            this.modelNotifier = modelNotifier
        }

        fun inMemory() = apply {
            inMemory = true
        }

        /**
         * @return Pass in dynamic database name here. Otherwise it defaults to class name.
         */
        fun databaseName(name: String) = apply {
            databaseName = name
        }

        /**
         * @return Pass in the extension for the DB here.
         * Otherwise defaults to ".db". If empty string passed, no extension is used.
         */
        fun extensionName(name: String) = apply {
            databaseExtensionName = name
        }

        fun build() = DatabaseConfig(this)
    }

    companion object {

        @JvmStatic
        fun builder(database: Class<*>, openHelperCreator: OpenHelperCreator): Builder =
            Builder(database, openHelperCreator)

        fun builder(database: KClass<*>, openHelperCreator: OpenHelperCreator): Builder =
            Builder(database, openHelperCreator)

        @JvmStatic
        fun inMemoryBuilder(database: Class<*>, openHelperCreator: OpenHelperCreator): Builder =
            Builder(database, openHelperCreator).inMemory()

        fun inMemoryBuilder(database: KClass<*>, openHelperCreator: OpenHelperCreator): Builder =
            Builder(database, openHelperCreator).inMemory()
    }
}
