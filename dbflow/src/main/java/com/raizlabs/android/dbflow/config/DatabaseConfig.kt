package com.raizlabs.android.dbflow.config

import com.raizlabs.android.dbflow.StringUtils
import com.raizlabs.android.dbflow.runtime.BaseTransactionManager
import com.raizlabs.android.dbflow.runtime.ModelNotifier
import com.raizlabs.android.dbflow.structure.database.DatabaseHelperListener
import com.raizlabs.android.dbflow.structure.database.OpenHelper
import java.util.*

/**
 * Description:
 */
class DatabaseConfig(
        val databaseClass: Class<*>,
        val openHelperCreator: ((DatabaseDefinition, DatabaseHelperListener?) -> OpenHelper)? = null,
        val transactionManagerCreator: ((DatabaseDefinition) -> BaseTransactionManager)? = null,
        val helperListener: DatabaseHelperListener? = null,
        val tableConfigMap: Map<Class<*>, TableConfig<*>> = mapOf(),
        val modelNotifier: ModelNotifier? = null,
        val isInMemory: Boolean = false,
        val databaseName: String? = null,
        val databaseExtensionName: String? = null) {


    interface OpenHelperCreator {

        fun createHelper(databaseDefinition: DatabaseDefinition, helperListener: DatabaseHelperListener?): OpenHelper
    }

    interface TransactionManagerCreator {

        fun createManager(databaseDefinition: DatabaseDefinition): BaseTransactionManager
    }

    internal constructor(builder: Builder) : this(
            // convert java interface to kotlin function.
            openHelperCreator = builder.openHelperCreator?.let {
                { databaseDefinition: DatabaseDefinition, helperListener: DatabaseHelperListener? ->
                    it.createHelper(databaseDefinition, helperListener)
                }
            },
            databaseClass = builder.databaseClass,
            transactionManagerCreator = builder.transactionManagerCreator?.let {
                { databaseDefinition: DatabaseDefinition ->
                    it.createManager(databaseDefinition)
                }
            },
            helperListener = builder.helperListener,
            tableConfigMap = builder.tableConfigMap,
            modelNotifier = builder.modelNotifier,
            isInMemory = builder.inMemory,
            databaseName = builder.databaseName ?: builder.databaseClass.simpleName,
            databaseExtensionName = when {
                builder.databaseExtensionName == null -> ".db"
                StringUtils.isNotNullOrEmpty(builder.databaseExtensionName) -> ".${builder.databaseExtensionName}"
                else -> ""
            })

    @Suppress("UNCHECKED_CAST")
    fun <TModel> getTableConfigForTable(modelClass: Class<TModel>): TableConfig<TModel>? {
        return tableConfigMap[modelClass] as TableConfig<TModel>?
    }

    /**
     * Build compatibility class for Java. Use the [DatabaseConfig] class directly if Kotlin consumer.
     */
    class Builder(internal val databaseClass: Class<*>) {

        internal var openHelperCreator: OpenHelperCreator? = null
        internal var transactionManagerCreator: TransactionManagerCreator? = null
        internal var helperListener: DatabaseHelperListener? = null
        internal val tableConfigMap: MutableMap<Class<*>, TableConfig<*>> = HashMap()
        internal var modelNotifier: ModelNotifier? = null
        internal var inMemory = false
        internal var databaseName: String? = null
        internal var databaseExtensionName: String? = null

        fun transactionManagerCreator(transactionManager: TransactionManagerCreator) = apply {
            this.transactionManagerCreator = transactionManager
        }

        fun helperListener(helperListener: DatabaseHelperListener) = apply {
            this.helperListener = helperListener
        }

        fun addTableConfig(tableConfig: TableConfig<*>) = apply {
            tableConfigMap.put(tableConfig.tableClass(), tableConfig)
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

        /**
         * Overrides the default [OpenHelper] for a [DatabaseDefinition].
         *
         * @param openHelper The openhelper to use.
         */
        fun openHelper(openHelper: OpenHelperCreator) = apply {
            openHelperCreator = openHelper
        }

        fun build() = DatabaseConfig(this)
    }

    companion object {

        @JvmStatic
        fun builder(database: Class<*>): DatabaseConfig.Builder {
            return DatabaseConfig.Builder(database)
        }

        @JvmStatic
        fun inMemoryBuilder(database: Class<*>): DatabaseConfig.Builder {
            return DatabaseConfig.Builder(database).inMemory()
        }
    }
}
