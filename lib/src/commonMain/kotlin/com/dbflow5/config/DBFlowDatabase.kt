package com.dbflow5.config

import com.dbflow5.adapter.DBRepresentable
import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.adapter.ViewAdapter
import com.dbflow5.annotation.Database
import com.dbflow5.annotation.opts.InternalDBFlowApi
import com.dbflow5.database.DatabaseCallback
import com.dbflow5.database.DatabaseStatement
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.database.FlowCursor
import com.dbflow5.database.Migration
import com.dbflow5.database.OpenHelper
import com.dbflow5.database.ThreadLocalTransaction
import com.dbflow5.database.config.DBPlatformSettings
import com.dbflow5.database.config.DBSettings
import com.dbflow5.database.scope.ReadableDatabaseScope
import com.dbflow5.database.scope.WritableDatabaseScope
import com.dbflow5.mpp.Closeable
import com.dbflow5.mpp.runBlocking
import com.dbflow5.observing.TableObserver
import com.dbflow5.runtime.ModelNotifier
import com.dbflow5.transaction.SuspendableTransaction
import com.dbflow5.transaction.Transaction
import com.dbflow5.transaction.TransactionDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.reflect.KClass

interface GeneratedDatabase : DatabaseWrapper, Closeable {
    override val generatedDatabase: GeneratedDatabase
        get() = this

    val platformSettings: DBPlatformSettings

    @InternalDBFlowApi
    val transactionDispatcher: TransactionDispatcher

    @InternalDBFlowApi
    val transactionId: ThreadLocalTransaction

    @InternalDBFlowApi
    val modelNotifier: ModelNotifier

    @InternalDBFlowApi
    val enqueueScope: CoroutineScope

    val writableScope: WritableDatabaseScope<GeneratedDatabase>

    val readableScope: ReadableDatabaseScope<GeneratedDatabase>

    @InternalDBFlowApi
    val isInMemory: Boolean

    val databaseFileName: String

    /**
     * If null is returned, the DB is assumed in memory.
     */
    val openHelperName: String?
        get() = databaseFileName.takeIf { !isInMemory }

    val databaseName: String

    /**
     * The current database version expected.
     */
    val databaseVersion: Int

    val writableDatabase: DatabaseWrapper

    @InternalDBFlowApi
    val tableObserver: TableObserver<DBFlowDatabase>

    /**
     * True if the [Database.foreignKeyConstraintsEnforced] annotation is true.
     */
    @InternalDBFlowApi
    val isForeignKeysSupported: Boolean

    @InternalDBFlowApi
    val throwExceptionsOnCreate: Boolean

    @InternalDBFlowApi
    val tables: List<KClass<*>>

    @InternalDBFlowApi
    val views: List<KClass<*>>

    @InternalDBFlowApi
    val queries: List<KClass<*>>

    @InternalDBFlowApi
    val migrations: Map<Int, List<Migration>>

    @InternalDBFlowApi
    val associatedDatabaseClassFile: KClass<*>


    override fun close()

    fun destroy()
}

/**
 * Description: The main interface that all Database implementations extend from. Use this to
 * pass in for operations and [Transaction].
 */
abstract class DBFlowDatabase : GeneratedDatabase {

    /**
     * Created by code generation. Do not implement this directly.
     */
    @InternalDBFlowApi
    abstract val settings: DBSettings

    @InternalDBFlowApi
    private val modelAdapters: List<ModelAdapter<*>> by lazy {
        tables.map { DatabaseObjectLookup.getModelAdapter(it) }
    }

    @InternalDBFlowApi
    private val viewAdapters: List<ViewAdapter<*>> by lazy {
        views.map { DatabaseObjectLookup.getModelViewAdapter(it) }
    }

    private val migrationMap = hashMapOf<Int, MutableList<Migration>>()

    /**
     * The helper that manages database changes and initialization
     */
    private var _openHelper: OpenHelper? = null

    /**
     * Allows for the app to listen for database changes.
     */
    private val callback: DatabaseCallback?
        get() = settings.databaseCallback

    override val platformSettings: DBPlatformSettings
        get() = settings.platformSettings

    /**
     * Used when resetting the DB
     */
    private var isResetting = false

    /**
     * Coroutine dispatcher, TODO: move to constructor / config.
     */
    override val transactionDispatcher: TransactionDispatcher by lazy {
        settings.transactionDispatcherFactory.create()
    }

    @InternalDBFlowApi
    override val transactionId: ThreadLocalTransaction = ThreadLocalTransaction()

    override val enqueueScope by lazy { CoroutineScope(transactionDispatcher.dispatcher) }

    override val migrations: Map<Int, List<Migration>>
        get() = migrationMap

    override val writableScope: WritableDatabaseScope<GeneratedDatabase> by lazy {
        WritableDatabaseScope(
            this
        )
    }
    override val readableScope: ReadableDatabaseScope<GeneratedDatabase> by lazy {
        ReadableDatabaseScope(this)
    }

    private var isOpened: Boolean = false

    val closeLock: Mutex = Mutex()

    internal var writeAheadLoggingEnabled = false

    override val isOpen: Boolean
        get() = isOpened

    val openHelper: OpenHelper by lazy {
        settings.openHelperCreator.createHelper(this, internalCallback)
            .also { onOpenWithConfig(settings, it) }
    }

    private fun onOpenWithConfig(settings: DBSettings, helper: OpenHelper) {
        runBlocking {
            helper.performRestoreFromBackup()

            val wal =
                settings.journalMode.adjustIfAutomatic(settings.platformSettings) == JournalMode.WriteAheadLogging
            helper.setWriteAheadLoggingEnabled(wal)
            writeAheadLoggingEnabled = wal
            isOpened = true
        }
    }

    override val writableDatabase: DatabaseWrapper
        get() = openHelper.database

    /**
     * @return The name of this database as defined in [Database]
     */
    override val databaseName: String
        get() = settings.name

    /**
     * @return The file name that this database points to
     */
    override val databaseFileName: String
        get() = databaseName + databaseExtensionName

    /**
     * @return the extension for the file name.
     */
    val databaseExtensionName: String
        get() = settings.databaseExtensionName

    /**
     * @return True if the database will reside in memory.
     */
    override val isInMemory: Boolean
        get() = settings.inMemory

    override val throwExceptionsOnCreate: Boolean
        get() = settings.throwExceptionsOnCreate

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
    override val tableObserver: TableObserver<DBFlowDatabase> by lazy {
        // observe all tables
        TableObserver(this, adapters = modelAdapters.toMutableList<DBRepresentable<*>>()
            .apply { addAll(viewAdapters) })
    }

    protected fun addMigration(version: Int, migration: Migration) {
        val list = migrationMap.getOrPut(version) { arrayListOf() }
        list += migration
    }

    override val modelNotifier: ModelNotifier by lazy {
        settings.modelNotifierFactory.create(this)
    }

    /**
     * Deletes the underlying database and destroys it.
     */
    override fun destroy() {
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
     * Closes the DB and stops the [TransactionDispatcher]
     */
    override fun close() {
        transactionDispatcher.dispatcher.cancel()
        if (isOpened) {
            runBlocking {
                closeLock.withLock {
                    openHelper.closeDB()
                    isOpened = false
                }
            }
        }
    }

    /**
     * Saves the database as a backup on the [enqueueScope]. This will
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
        override fun onOpen(db: DatabaseWrapper) {
            tableObserver.construct(db)
            callback?.onOpen(db)
        }

        override fun onCreate(db: DatabaseWrapper) {
            callback?.onCreate(db)
        }

        override fun onUpgrade(database: DatabaseWrapper, oldVersion: Int, newVersion: Int) {
            callback?.onUpgrade(database, oldVersion, newVersion)
        }

        override fun onDowngrade(
            db: DatabaseWrapper,
            oldVersion: Int,
            newVersion: Int
        ) {
            callback?.onDowngrade(db, oldVersion, newVersion)
        }

        override fun onConfigure(db: DatabaseWrapper) {
            callback?.onConfigure(db)
        }
    }
}

fun <DB : GeneratedDatabase, R : Any?> DB.beginTransactionAsync(transaction: SuspendableTransaction<DB, R>):
    Transaction.Builder<DB, R> =
    Transaction.Builder(transaction, this)

/**
 * Runs the transaction within the [TransactionDispatcher]
 */
suspend fun <DB : GeneratedDatabase, R> DB.executeTransaction(transaction: SuspendableTransaction<DB, R>): R =
    transactionDispatcher.executeTransaction(this, transaction)

/**
 * Enqueues the transaction without waiting for the result.
 */
fun <DB : GeneratedDatabase, R> DB.enqueueTransaction(transaction: SuspendableTransaction<DB, R>): Job {
    return enqueueScope.launch {
        executeTransaction(transaction)
    }
}

@Suppress("unchecked_cast")
internal suspend fun <DB : GeneratedDatabase, R> WritableDatabaseScope<DB>.executeTransactionForResult(
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
suspend inline fun <reified DB : GeneratedDatabase, R> DB.writableTransaction(
    crossinline fn: suspend WritableDatabaseScope<DB>.() -> R
): R = executeTransaction { fn() }

/**
 * Runs the [fn] block within the [ReadableDatabaseScope] asynchronously using coroutines.
 * Only allows readable operations such as queries.
 */
suspend inline fun <reified DB : GeneratedDatabase, R> DB.readableTransaction(
    crossinline fn: suspend ReadableDatabaseScope<DB>.() -> R
): R = executeTransaction { fn() }
