package com.dbflow5.config

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import com.dbflow5.annotation.Database
import com.dbflow5.database.AndroidSQLiteOpenHelper
import com.dbflow5.database.DatabaseCallback
import com.dbflow5.database.DatabaseStatement
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.database.FlowCursor
import com.dbflow5.database.OpenHelper
import com.dbflow5.database.scope.ReadableDatabaseScope
import com.dbflow5.database.scope.WritableDatabaseScope
import com.dbflow5.migration.Migration
import com.dbflow5.observing.TableObserver
import com.dbflow5.runtime.DirectModelNotifier
import com.dbflow5.runtime.ModelNotifier
import com.dbflow5.transaction.SuspendableTransaction
import com.dbflow5.transaction.Transaction
import com.dbflow5.transaction.TransactionDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.reflect.KClass

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
                // check if low ram device
                val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
                if (manager?.isLowRamDevice == false) {
                    WriteAheadLogging
                } else {
                    Truncate
                }
            }
        }
    }

    override val associatedDBFlowDatabase: DBFlowDatabase
        get() = this

    private val migrationMap = hashMapOf<Int, MutableList<Migration>>()

    /**
     * The helper that manages database changes and initialization
     */
    private var _openHelper: OpenHelper? = null

    /**
     * Allows for the app to listen for database changes.
     */
    private val callback: DatabaseCallback? by lazy { databaseConfig?.callback }

    /**
     * Used when resetting the DB
     */
    private var isResetting = false

    /**
     * Coroutine dispatcher, TODO: move to constructor / config.
     */
    internal val transactionDispatcher: TransactionDispatcher by lazy {
        databaseConfig.let { databaseConfig ->
            if (databaseConfig?.transactionDispatcherFactory == null) {
                TransactionDispatcher()
            } else {
                databaseConfig.transactionDispatcherFactory.create()
            }
        }
    }

    @VisibleForTesting
    val dispatcher: TransactionDispatcher
        get() = transactionDispatcher

    internal val enqueueScope by lazy { CoroutineScope(transactionDispatcher.dispatcher) }

    private var databaseConfig: DatabaseConfig? by MutableLazy {
        FlowManager.getConfig().databaseConfigMap[associatedDatabaseClassFile]
    }

    private var modelNotifier: ModelNotifier? = null

    abstract val tables: List<KClass<*>>
    abstract val views: List<KClass<*>>
    abstract val queries: List<KClass<*>>

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

    val openHelper: OpenHelper by lazy {
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
        return@lazy helper
    }

    private fun onOpenWithConfig(config: DatabaseConfig?, helper: OpenHelper) {
        runBlocking {
            helper.performRestoreFromBackup()

            val wal = config != null &&
                config.journalMode.adjustIfAutomatic(FlowManager.context) == JournalMode.WriteAheadLogging
            helper.setWriteAheadLoggingEnabled(wal)
            writeAheadLoggingEnabled = wal
            isOpened = true
        }
    }

    val writableDatabase: DatabaseWrapper
        get() = openHelper.database

    /**
     * @return The name of this database as defined in [Database]
     */
    val databaseName: String
        get() = databaseConfig?.databaseName ?: associatedDatabaseClassFile.simpleName!!

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

    internal val throwExceptionsOnCreate
        get() = databaseConfig?.throwExceptionsOnCreate ?: true

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
    abstract val associatedDatabaseClassFile: KClass<*>

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
    val tableObserver: TableObserver<DBFlowDatabase> by lazy {
        // observe all tables
        TableObserver(this, tables = tables.toMutableList().apply {
            addAll(views)
        })
    }

    /**
     * Applies a database configuration object to this class.
     */
    @Suppress("UNCHECKED_CAST")
    private fun applyDatabaseConfig(databaseConfig: DatabaseConfig?) {
        if (databaseConfig != null) {
            // TODO: figure out configuration solution for multiple DBs.
            /* // initialize configuration if exists.
             val tableConfigCollection = databaseConfig.tableConfigMap.values
             for (tableConfig in tableConfigCollection) {
                 val modelAdapter: ModelAdapter<Any> =
                     modelAdapterMap[tableConfig.tableClass] as ModelAdapter<Any>?
                         ?: continue
                 tableConfig.listModelLoader?.let { loader ->
                     modelAdapter.listModelLoader = loader as ListModelLoader<Any>
                 }
                 tableConfig.singleModelLoader?.let { loader ->
                     modelAdapter.singleModelLoader = loader as SingleModelLoader<Any>
                 }
                 tableConfig.modelSaver?.let { saver ->
                     modelAdapter.modelSaver = saver as ModelSaver<Any>
                 }
             }*/
        }
    }

    protected fun addMigration(version: Int, migration: Migration) {
        val list = migrationMap.getOrPut(version) { arrayListOf() }
        list += migration
    }

    fun getModelNotifier(): ModelNotifier {
        var notifier = modelNotifier
        if (notifier == null) {
            val config = FlowManager.getConfig().databaseConfigMap[associatedDatabaseClassFile]
            notifier = if (config?.modelNotifier == null) {
                DirectModelNotifier.get(this)
            } else {
                config.modelNotifier.invoke(this)
            }
        }
        modelNotifier = notifier
        return notifier
    }

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
            this.databaseConfig = databaseConfig
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
            this.databaseConfig = databaseConfig
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
        transactionDispatcher.dispatcher.cancel()
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
        enqueueScope.launch {
            openHelper.backupDB()
        }
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

    override fun compileStatement(rawQuery: String): DatabaseStatement =
        writableDatabase.compileStatement(rawQuery)

    override fun rawQuery(query: String, selectionArgs: Array<String>?): FlowCursor =
        writableDatabase.rawQuery(query, selectionArgs)

    override fun delete(tableName: String, whereClause: String?, whereArgs: Array<String>?): Int =
        writableDatabase.delete(tableName, whereClause, whereArgs)

    override fun query(
        tableName: String,
        columns: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        groupBy: String?, having: String?,
        orderBy: String?
    ): FlowCursor = writableDatabase.query(
        tableName,
        columns,
        selection,
        selectionArgs,
        groupBy,
        having,
        orderBy
    )

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

        override fun onDowngrade(
            databaseWrapper: DatabaseWrapper,
            oldVersion: Int,
            newVersion: Int
        ) {
            callback?.onDowngrade(databaseWrapper, oldVersion, newVersion)
        }

        override fun onConfigure(db: DatabaseWrapper) {
            callback?.onConfigure(db)
        }
    }
}

fun <DB : DBFlowDatabase, R : Any?> DB.beginTransactionAsync(transaction: SuspendableTransaction<DB, R>):
    Transaction.Builder<DB, R> =
    Transaction.Builder(transaction, this)

/**
 * Runs the transaction within the [transactionDispatcher]
 */
suspend fun <DB : DBFlowDatabase, R> DB.executeTransaction(transaction: SuspendableTransaction<DB, R>): R =
    transactionDispatcher.executeTransaction(this, transaction)

/**
 * Enqueues the transaction without waiting for the result.
 */
fun <DB : DBFlowDatabase, R> DB.enqueueTransaction(transaction: SuspendableTransaction<DB, R>): Job {
    return enqueueScope.launch {
        executeTransaction(transaction)
    }
}

@Suppress("unchecked_cast")
internal suspend fun <DB : DBFlowDatabase, R> WritableDatabaseScope<DB>.executeTransactionForResult(
    transaction: SuspendableTransaction<DB, R>
): R {
    // if we use transaction, let the class define if we want DB transaction.
    return if (transaction is Transaction<*, *>) {
        transaction.run { this@executeTransactionForResult.execute() } as R
    } else try {
        db.beginTransaction()
        val result = transaction.run { this@executeTransactionForResult.execute() }
        db.setTransactionSuccessful()
        result
    } finally {
        db.endTransaction()
    }
}

/**
 * Runs the [fn] block within the [WritableDatabaseScope] asynchronously using coroutines.
 * Enables DB writing which may block until ability to write.
 */
suspend inline fun <reified DB : DBFlowDatabase, R> DB.writableTransaction(
    crossinline fn: suspend WritableDatabaseScope<DB>.() -> R
): R = executeTransaction { fn() }

/**
 * Runs the [fn] block within the [ReadableDatabaseScope] asynchronously using coroutines.
 * Only allows readable operations such as queries.
 */
suspend inline fun <reified DB : DBFlowDatabase, R> DB.readableTransaction(
    crossinline fn: suspend ReadableDatabaseScope<DB>.() -> R
): R = executeTransaction { fn() }
