package com.dbflow5.config

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.adapter.ModelViewAdapter
import com.dbflow5.adapter.RetrievalAdapter
import com.dbflow5.adapter.queriable.ListModelLoader
import com.dbflow5.adapter.queriable.SingleModelLoader
import com.dbflow5.adapter.saveable.ModelSaver
import com.dbflow5.annotation.Database
import com.dbflow5.annotation.QueryModel
import com.dbflow5.annotation.Table
import com.dbflow5.database.AndroidSQLiteOpenHelper
import com.dbflow5.database.DatabaseCallback
import com.dbflow5.database.DatabaseStatement
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.database.FlowCursor
import com.dbflow5.database.OpenHelper
import com.dbflow5.database.trySetWriteAheadLoggingEnabled
import com.dbflow5.migration.Migration
import com.dbflow5.observing.TableObserver
import com.dbflow5.runtime.DirectModelNotifier
import com.dbflow5.runtime.ModelNotifier
import com.dbflow5.structure.BaseModelView
import com.dbflow5.transaction.BaseTransactionManager
import com.dbflow5.transaction.DefaultTransactionManager
import com.dbflow5.transaction.DefaultTransactionQueue
import com.dbflow5.transaction.ITransaction
import com.dbflow5.transaction.Transaction
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

/**
 * Description: The main interface that all Database implementations extend from. Use this to
 * pass in for operations and [Transaction].
 */
abstract class DBFlowDatabase : DatabaseWrapper {

    enum class JournalMode {
        Automatic,
        Truncate,

        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
        WriteAheadLogging;

        fun adjustIfAutomatic(context: Context): JournalMode = when (this) {
            Automatic -> this
            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    // check if low ram device
                    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && manager?.isLowRamDevice == false) {
                        WriteAheadLogging
                    }
                }
                Truncate
            }
        }
    }

    private val migrationMap = hashMapOf<Int, MutableList<Migration>>()

    private val modelAdapterMap = hashMapOf<Class<*>, ModelAdapter<*>>()

    private val modelTableNames = hashMapOf<String, Class<*>>()

    private val modelViewAdapterMap = linkedMapOf<Class<*>, ModelViewAdapter<*>>()

    private val queryModelAdapterMap = linkedMapOf<Class<*>, RetrievalAdapter<*>>()

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
    val modelClasses: List<Class<*>>
        get() = modelAdapterMap.keys.toList()

    /**
     * @return the [BaseModelView] list for this database.
     */
    val modelViews: List<Class<*>>
        get() = modelViewAdapterMap.keys.toList()

    /**
     * @return The list of [ModelViewAdapter]. Internal method for
     * creating model views in the DB.
     */
    val modelViewAdapters: List<ModelViewAdapter<*>>
        get() = modelViewAdapterMap.values.toList()

    /**
     * @return The list of [RetrievalAdapter]. Internal method for creating query models in the DB.
     */
    val queryModelAdapters: List<RetrievalAdapter<*>>
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

    val closeLock: Lock = ReentrantLock()

    internal var writeAheadLoggingEnabled = false

    val openHelper: OpenHelper
        @Synchronized get() {
            var helper = _openHelper
            if (helper == null) {
                val config = FlowManager.getConfig().databaseConfigMap[associatedDatabaseClassFile]
                helper = if (config?.openHelperCreator != null) {
                    config.openHelperCreator.createHelper(this, internalCallback)
                } else {
                    AndroidSQLiteOpenHelper(FlowManager.context, this, internalCallback)
                }
                onOpenWithConfig(config, helper)
            }
            _openHelper = helper
            return helper
        }

    private fun onOpenWithConfig(config: DatabaseConfig?, helper: OpenHelper) {
        var wal = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            wal = config != null && config.journalMode.adjustIfAutomatic(FlowManager.context) == JournalMode.WriteAheadLogging
            helper.trySetWriteAheadLoggingEnabled(wal)
        }
        writeAheadLoggingEnabled = wal
        helper.performRestoreFromBackup()
        isOpened = true
    }

    val writableDatabase: DatabaseWrapper
        get() = openHelper.database

    /**
     * @return The name of this database as defined in [Database]
     */
    val databaseName: String
        get() = databaseConfig?.databaseName ?: associatedDatabaseClassFile.simpleName

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
    abstract val associatedDatabaseClassFile: Class<*>

    /**
     * @return True if the database is ok. If backups are enabled, we restore from backup and will
     * override the return value if it replaces the main DB.
     */
    val isDatabaseIntegrityOk: Boolean
        get() = openHelper.isDatabaseIntegrityOk

    /**
     * Returns the associated table observer that tracks changes to tables during transactions on
     * the DB.
     */
    val tableObserver: TableObserver by lazy {
        // observe all tables
        TableObserver(this, tables = modelClasses.toMutableList().apply {
            addAll(modelViews)
        })
    }

    init {
        @Suppress("LeakingThis")
        applyDatabaseConfig(FlowManager.getConfig().databaseConfigMap[associatedDatabaseClassFile])
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
                val modelAdapter: ModelAdapter<Any> = modelAdapterMap[tableConfig.tableClass] as ModelAdapter<Any>?
                    ?: continue
                tableConfig.listModelLoader?.let { loader -> modelAdapter.listModelLoader = loader as ListModelLoader<Any> }
                tableConfig.singleModelLoader?.let { loader -> modelAdapter.singleModelLoader = loader as SingleModelLoader<Any> }
                tableConfig.modelSaver?.let { saver -> modelAdapter.modelSaver = saver as ModelSaver<Any> }
            }
            callback = databaseConfig.callback
        }
        transactionManager = if (databaseConfig?.transactionManagerCreator == null) {
            DefaultTransactionManager(this)
        } else {
            databaseConfig.transactionManagerCreator.createManager(this)
        }
    }

    protected fun <T : Any> addModelAdapter(modelAdapter: ModelAdapter<T>, holder: DatabaseHolder) {
        holder.putDatabaseForTable(modelAdapter.table, this)
        modelTableNames[modelAdapter.name] = modelAdapter.table
        modelAdapterMap[modelAdapter.table] = modelAdapter
    }

    protected fun <T : Any> addModelViewAdapter(modelViewAdapter: ModelViewAdapter<T>, holder: DatabaseHolder) {
        holder.putDatabaseForTable(modelViewAdapter.table, this)
        modelViewAdapterMap[modelViewAdapter.table] = modelViewAdapter
    }

    protected fun <T : Any> addRetrievalAdapter(retrievalAdapter: RetrievalAdapter<T>, holder: DatabaseHolder) {
        holder.putDatabaseForTable(retrievalAdapter.table, this)
        queryModelAdapterMap[retrievalAdapter.table] = retrievalAdapter
    }

    protected fun addMigration(version: Int, migration: Migration) {
        val list = migrationMap.getOrPut(version) { arrayListOf() }
        list += migration
    }

    /**
     * Internal method used to create the database schema.
     *
     * @return List of Model Adapters
     */
    val modelAdapters: List<ModelAdapter<*>>
        get() = modelAdapterMap.values.toList()

    /**
     * Returns the associated [ModelAdapter] within this database for
     * the specified table. If the Model is missing the [Table] annotation,
     * this will return null.
     *
     * @param table The model that exists in this database.
     * @return The ModelAdapter for the table.
     */
    fun <T : Any> getModelAdapterForTable(table: Class<T>): ModelAdapter<T>? {
        @Suppress("UNCHECKED_CAST")
        return modelAdapterMap[table] as ModelAdapter<T>?
    }

    /**
     * @param tableName The name of the table in this db.
     * @return The associated [ModelAdapter] within this database for the specified table name.
     * If the Model is missing the [Table] annotation, this will return null.
     */
    fun getModelClassForName(tableName: String): Class<*>? = modelTableNames[tableName]

    /**
     * @param table the VIEW class to retrieve the ModelViewAdapter from.
     * @return the associated [ModelViewAdapter] for the specified table.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getModelViewAdapterForTable(table: Class<T>): ModelViewAdapter<T>? =
        modelViewAdapterMap[table] as ModelViewAdapter<T>?

    /**
     * @param queryModel The [QueryModel] class
     * @return The adapter that corresponds to the specified class.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getQueryModelAdapterForQueryClass(queryModel: Class<T>): RetrievalAdapter<T>? =
        queryModelAdapterMap[queryModel] as RetrievalAdapter<T>?

    fun getModelNotifier(): ModelNotifier {
        var notifier = modelNotifier
        if (notifier == null) {
            val config = FlowManager.getConfig().databaseConfigMap[associatedDatabaseClassFile]
            notifier = if (config?.modelNotifier == null) {
                DirectModelNotifier()
            } else {
                config.modelNotifier
            }
        }
        modelNotifier = notifier
        return notifier
    }

    fun <R : Any?> beginTransactionAsync(transaction: ITransaction<R>): Transaction.Builder<R> =
        Transaction.Builder(transaction, this)

    inline fun <R : Any?> beginTransactionAsync(crossinline transaction: (DatabaseWrapper) -> R): Transaction.Builder<R> =
        beginTransactionAsync(object : ITransaction<R> {
            override fun execute(databaseWrapper: DatabaseWrapper) = transaction(databaseWrapper)
        })

    /**
     * This should never get called on the main thread. Use [beginTransactionAsync] for an async-variant.
     * Runs a transaction in the current thread.
     */
    @WorkerThread
    fun <R> executeTransaction(transaction: ITransaction<R>): R {
        try {
            beginTransaction()
            val result = transaction.execute(writableDatabase)
            setTransactionSuccessful()
            return result
        } finally {
            endTransaction()
        }
    }

    /**
     * This should never get called on the main thread. Use [beginTransactionAsync] for an async-variant.
     * Runs a transaction in the current thread.
     */
    @WorkerThread
    inline fun <R> executeTransaction(crossinline transaction: (DatabaseWrapper) -> R) = executeTransaction(object : ITransaction<R> {
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
            try {
                closeLock.lock()
                openHelper.closeDB()
                isOpened = false
            } finally {
                closeLock.unlock()
            }
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

    override fun beginTransaction() {
        tableObserver.syncTriggers(writableDatabase)
        writableDatabase.beginTransaction()
    }

    override fun setTransactionSuccessful() = writableDatabase.setTransactionSuccessful()

    override fun endTransaction() {
        writableDatabase.endTransaction()
        if (!isInTransaction) {
            tableObserver.enqueueTableUpdateCheck()
        }
    }

    override fun compileStatement(rawQuery: String): DatabaseStatement = writableDatabase.compileStatement(rawQuery)

    override fun rawQuery(query: String, selectionArgs: Array<String>?): FlowCursor = writableDatabase.rawQuery(query, selectionArgs)

    override fun delete(tableName: String, whereClause: String?, whereArgs: Array<String>?): Int = writableDatabase.delete(tableName, whereClause, whereArgs)

    override fun query(tableName: String,
                       columns: Array<String>?,
                       selection: String?,
                       selectionArgs: Array<String>?,
                       groupBy: String?, having: String?,
                       orderBy: String?): FlowCursor = writableDatabase.query(tableName, columns, selection, selectionArgs, groupBy, having, orderBy)

    override val isInTransaction: Boolean
        get() = writableDatabase.isInTransaction

    private val internalCallback: DatabaseCallback = object : DatabaseCallback {
        override fun onOpen(database: DatabaseWrapper) {
            tableObserver.construct(database)
            callback?.onOpen(database)
        }

        override fun onCreate(database: DatabaseWrapper) {
            callback?.onCreate(database)
        }

        override fun onUpgrade(database: DatabaseWrapper, oldVersion: Int, newVersion: Int) {
            callback?.onUpgrade(database, oldVersion, newVersion)
        }

        override fun onDowngrade(databaseWrapper: DatabaseWrapper, oldVersion: Int, newVersion: Int) {
            callback?.onDowngrade(databaseWrapper, oldVersion, newVersion)
        }
    }
}
