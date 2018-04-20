package com.raizlabs.dbflow5.config

import com.raizlabs.dbflow5.JvmOverloads
import com.raizlabs.dbflow5.Synchronized
import com.raizlabs.dbflow5.adapter.ModelAdapter
import com.raizlabs.dbflow5.adapter.ModelViewAdapter
import com.raizlabs.dbflow5.adapter.QueryModelAdapter
import com.raizlabs.dbflow5.adapter.queriable.ListModelLoader
import com.raizlabs.dbflow5.adapter.queriable.SingleModelLoader
import com.raizlabs.dbflow5.adapter.saveable.ModelSaver
import com.raizlabs.dbflow5.database.*
import com.raizlabs.dbflow5.migration.Migration
import com.raizlabs.dbflow5.runtime.DirectModelNotifier
import com.raizlabs.dbflow5.runtime.ModelNotifier
import com.raizlabs.dbflow5.structure.BaseModelView
import com.raizlabs.dbflow5.structure.InvalidDBConfiguration
import com.raizlabs.dbflow5.transaction.*
import kotlin.reflect.KClass

expect abstract class DBFlowDatabase : InternalDBFlowDatabase

/**
 * Description: The main interface that all Database implementations extend from. Use this to
 * pass in for operations and [Transaction].
 */
abstract class InternalDBFlowDatabase : DatabaseWrapper {

    private val migrationMap = hashMapOf<Int, MutableList<Migration>>()

    private val modelAdapters = hashMapOf<KClass<*>, ModelAdapter<*>>()

    private val modelTableNames = hashMapOf<String, KClass<*>>()

    private val modelViewAdapterMap = linkedMapOf<KClass<*>, ModelViewAdapter<*>>()

    private val queryModelAdapterMap = linkedMapOf<KClass<*>, QueryModelAdapter<*>>()

    /**
     * The helper that manages database changes and initialization
     */
    private var _openHelper: OpenHelper? = null

    /**
     * Allows for the app to listen for database changes.
     */
    private var callback: DatabaseCallback? = null

    /**
     * Used when resetting the DB
     */
    private var isResetting = false

    lateinit var transactionManager: BaseTransactionManager
        private set

    private var databaseConfig: DatabaseConfig? = null

    private var modelNotifier: ModelNotifier? = null

    /**
     * @return a list of all model classes in this database.
     */
    val modelClasses: List<KClass<*>>
        get() = modelAdapters.keys.toList()

    /**
     * @return the [BaseModelView] list for this database.
     */
    val modelViews: List<KClass<*>>
        get() = modelViewAdapterMap.keys.toList()

    /**
     * @return The list of [ModelViewAdapter]. Internal method for
     * creating model views in the DB.
     */
    val modelViewAdapters: List<ModelViewAdapter<*>>
        get() = modelViewAdapterMap.values.toList()

    /**
     * @return The list of [QueryModelAdapter]. Internal method for creating query models in the DB.
     */
    val modelQueryAdapters: List<QueryModelAdapter<*>>
        get() = queryModelAdapterMap.values.toList()

    /**
     * @return The map of migrations to DB version
     */
    val migrations: Map<Int, List<Migration>>
        get() = migrationMap

    /**
     * Returns true if the [openHelper] has been created.
     */
    var isOpened: Boolean = false

    val openHelper: OpenHelper
        @Synchronized
        get() {
            var helper = _openHelper
            if (helper == null) {
                val config = FlowManager.getConfig().databaseConfigMap[associatedDatabaseKClassFile]
                helper = if (config?.openHelperCreator != null) {
                    config.openHelperCreator.invoke(this as DBFlowDatabase, callback)
                } else {
                    PlatformOpenHelper(this as DBFlowDatabase, callback)
                }
                helper.performRestoreFromBackup()
                isOpened = true
            }
            _openHelper = helper
            return helper
        }

    val writableDatabase: DatabaseWrapper
        get() = openHelper.database

    /**
     * @return The name of this database as defined in [Database]
     */
    val databaseName: String
        get() = databaseConfig?.databaseName
            ?: throw InvalidDBConfiguration("Database name must be specified in the DatabaseConfig.")

    /**
     * @return The file name that this database points to
     */
    val databaseFileName: String
        get() = databaseName + databaseExtensionName

    /**
     * @return the extension for the file name.
     */
    val databaseExtensionName: String
        get() = databaseConfig?.databaseExtensionName ?: ".db"

    /**
     * @return True if the database will reside in memory.
     */
    val isInMemory: Boolean
        get() = databaseConfig?.isInMemory ?: false

    /**
     * @return The version of the database currently.
     */
    abstract val databaseVersion: Int

    /**
     * @return True if the [Database.foreignKeyConstraintsEnforced] annotation is true.
     */
    abstract val isForeignKeysSupported: Boolean

    /**
     * @return The class that defines the [Database] annotation.
     */
    abstract val associatedDatabaseKClassFile: KClass<*>

    /**
     * @return True if the database is ok. If backups are enabled, we restore from backup and will
     * override the return value if it replaces the main DB.
     */
    val isDatabaseIntegrityOk: Boolean
        get() = openHelper.isDatabaseIntegrityOk

    init {
        @Suppress("LeakingThis")
        applyDatabaseConfig(FlowManager.getConfig().databaseConfigMap[associatedDatabaseKClassFile])
    }

    /**
     * Applies a database configuration object to this class.
     */
    @Suppress("UNCHECKED_CAST")
    private fun applyDatabaseConfig(databaseConfig: DatabaseConfig?) {
        this.databaseConfig = databaseConfig
        if (databaseConfig != null) {
            // initialize configuration if exists.
            val tableConfigCollection = databaseConfig.tableConfigMap.values
            for (tableConfig in tableConfigCollection) {
                val modelAdapter: ModelAdapter<Any> = modelAdapters[tableConfig.tableClass] as ModelAdapter<Any>?
                    ?: continue
                tableConfig.listModelLoader?.let { modelAdapter.listModelLoader = it as ListModelLoader<Any> }
                tableConfig.singleModelLoader?.let { modelAdapter.singleModelLoader = it as SingleModelLoader<Any> }
                tableConfig.modelSaver?.let { modelAdapter.modelSaver = it as ModelSaver<Any> }
            }
            callback = databaseConfig.callback
        }
        transactionManager = if (databaseConfig?.transactionManagerCreator == null) {
            DefaultTransactionManager(this as DBFlowDatabase)
        } else {
            databaseConfig.transactionManagerCreator.invoke(this as DBFlowDatabase)
        }
    }

    protected fun <T : Any> addModelAdapter(modelAdapter: ModelAdapter<T>, holder: DatabaseHolder) {
        holder.putDatabaseForTable(modelAdapter.kTable, this as DBFlowDatabase)
        modelTableNames.put(modelAdapter.tableName, modelAdapter.kTable)
        modelAdapters.put(modelAdapter.kTable, modelAdapter)
    }

    protected fun <T : Any> addModelViewAdapter(modelViewAdapter: ModelViewAdapter<T>, holder: DatabaseHolder) {
        holder.putDatabaseForTable(modelViewAdapter.kTable, this as DBFlowDatabase)
        modelViewAdapterMap.put(modelViewAdapter.kTable, modelViewAdapter)
    }

    protected fun <T : Any> addQueryModelAdapter(queryModelAdapter: QueryModelAdapter<T>, holder: DatabaseHolder) {
        holder.putDatabaseForTable(queryModelAdapter.kTable, this as DBFlowDatabase)
        queryModelAdapterMap.put(queryModelAdapter.kTable, queryModelAdapter)
    }

    protected fun addMigration(version: Int, migration: Migration) {
        var list: MutableList<Migration>? = migrationMap[version]
        if (list == null) {
            list = arrayListOf()
            migrationMap.put(version, list)
        }
        list.add(migration)
    }

    /**
     * Internal method used to create the database schema.
     *
     * @return List of Model Adapters
     */
    fun getModelAdapters(): List<ModelAdapter<*>> = modelAdapters.values.toList()

    /**
     * Returns the associated [ModelAdapter] within this database for
     * the specified table. If the Model is missing the [Table] annotation,
     * this will return null.
     *
     * @param table The model that exists in this database.
     * @return The ModelAdapter for the table.
     */
    fun <T : Any> getModelAdapterForTable(table: KClass<T>): ModelAdapter<T>? {
        @Suppress("UNCHECKED_CAST")
        return modelAdapters[table] as ModelAdapter<T>?
    }

    /**
     * @param tableName The name of the table in this db.
     * @return The associated [ModelAdapter] within this database for the specified table name.
     * If the Model is missing the [Table] annotation, this will return null.
     */
    fun getModelClassForName(tableName: String): KClass<*>? = modelTableNames[tableName]

    /**
     * @param table the VIEW class to retrieve the ModelViewAdapter from.
     * @return the associated [ModelViewAdapter] for the specified table.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getModelViewAdapterForTable(table: KClass<T>): ModelViewAdapter<T>? =
        modelViewAdapterMap[table] as ModelViewAdapter<T>?

    /**
     * @param queryModel The [QueryModel] class
     * @return The adapter that corresponds to the specified class.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getQueryModelAdapterForQueryClass(queryModel: KClass<T>): QueryModelAdapter<T>? =
        queryModelAdapterMap[queryModel] as QueryModelAdapter<T>?

    fun getModelNotifier(): ModelNotifier {
        var notifier = modelNotifier
        if (notifier == null) {
            val config = FlowManager.getConfig().databaseConfigMap[associatedDatabaseKClassFile]
            notifier = if (config?.modelNotifier == null) {
                DirectModelNotifier()
            } else {
                config.modelNotifier
            }
        }
        modelNotifier = notifier
        return notifier
    }

    /**
     * Executes and returns the executed transaction.
     */
    fun <R : Any?> executeTransactionAsync(transaction: ITransaction<R>,
                                           success: ((Transaction<R>, R) -> Unit)? = null,
                                           error: ((Transaction<R>, Throwable) -> Unit)? = null): Transaction<R> = beginTransactionAsync(transaction)
        .success(success)
        .error(error)
        .execute()

    /**
     * Executes and returns the executed transaction.
     */
    fun <R : Any?> executeTransactionAsync(transaction: (DatabaseWrapper) -> R,
                                           success: ((Transaction<R>, R) -> Unit)? = null,
                                           error: ((Transaction<R>, Throwable) -> Unit)? = null): Transaction<R> = beginTransactionAsync(transaction)
        .success(success)
        .error(error)
        .execute()

    fun <R : Any?> beginTransactionAsync(transaction: ITransaction<R>): Transaction.Builder<R> =
        Transaction.Builder(transaction, this as DBFlowDatabase)

    fun <R : Any?> beginTransactionAsync(transaction: (DatabaseWrapper) -> R): Transaction.Builder<R> =
        beginTransactionAsync(object : ITransaction<R> {
            override fun execute(databaseWrapper: DatabaseWrapper) = transaction(databaseWrapper)
        })

    /**
     * @return True if the [Database.consistencyCheckEnabled] annotation is true.
     */
    abstract fun areConsistencyChecksEnabled(): Boolean

    /**
     * @return True if the [Database.backupEnabled] annotation is true.
     */
    abstract fun backupEnabled(): Boolean

    /**
     * Performs a full deletion of this database. Reopens the [AndroidSQLiteOpenHelper] as well.
     *
     * Reapplies the [DatabaseConfig] if we have one.
     * @param databaseConfig sets a new [DatabaseConfig] on this class.
     */
    @JvmOverloads
    fun reset(databaseConfig: DatabaseConfig? = this.databaseConfig) {
        if (!isResetting) {
            destroy()
            // reapply configuration before opening it.
            applyDatabaseConfig(databaseConfig)
            openHelper.database
        }
    }

    /**
     * Reopens the DB with the new [DatabaseConfig] specified.
     * Reapplies the [DatabaseConfig] if we have one.
     *
     * @param databaseConfig sets a new [DatabaseConfig] on this class.
     */
    @JvmOverloads
    fun reopen(databaseConfig: DatabaseConfig? = this.databaseConfig) {
        if (!isResetting) {
            close()
            _openHelper = null
            isOpened = false
            applyDatabaseConfig(databaseConfig)
            openHelper.database
            isResetting = false
        }
    }

    /**
     * Deletes the underlying database and destroys it.
     */
    fun destroy() {
        if (!isResetting) {
            isResetting = true
            close()
            openHelper.deleteDB()
            _openHelper = null
            isOpened = false
            isResetting = false
        }
    }

    /**
     * Closes the DB and stops the [BaseTransactionManager]
     */
    fun close() {
        transactionManager.stopQueue()
        if (isOpened) {
            openHelper.closeDB()
            isOpened = false
        }
    }

    /**
     * Saves the database as a backup on the [DefaultTransactionQueue]. This will
     * create a THIRD database to use as a backup to the backup in case somehow the overwrite fails.
     *
     * @throws java.lang.IllegalStateException if [Database.backupEnabled]
     * or [Database.consistencyCheckEnabled] is not enabled.
     */
    fun backupDatabase() {
        openHelper.backupDB()
    }

    override val version: Int
        get() = writableDatabase.version

    override fun execSQL(query: String) = writableDatabase.execSQL(query)

    override fun beginTransaction() = writableDatabase.beginTransaction()

    override fun setTransactionSuccessful() = writableDatabase.setTransactionSuccessful()

    override fun endTransaction() = writableDatabase.endTransaction()

    override fun compileStatement(rawQuery: String): DatabaseStatement = writableDatabase.compileStatement(rawQuery)

    override fun rawQuery(query: String, selectionArgs: Array<String>?): FlowCursor = writableDatabase.rawQuery(query, selectionArgs)

    override fun delete(tableName: String, whereClause: String?, whereArgs: Array<String>?): Int = writableDatabase.delete(tableName, whereClause, whereArgs)

    override fun query(tableName: String,
                       columns: Array<String>?,
                       selection: String?,
                       selectionArgs: Array<String>?,
                       groupBy: String?, having: String?,
                       orderBy: String?): FlowCursor = writableDatabase.query(tableName, columns, selection, selectionArgs, groupBy, having, orderBy)
}
