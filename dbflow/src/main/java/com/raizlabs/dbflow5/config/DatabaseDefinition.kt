package com.raizlabs.dbflow5.config

import android.content.ContentValues
import android.content.Context
import com.raizlabs.dbflow5.adapter.ModelAdapter
import com.raizlabs.dbflow5.adapter.ModelViewAdapter
import com.raizlabs.dbflow5.adapter.QueryModelAdapter
import com.raizlabs.dbflow5.adapter.queriable.ListModelLoader
import com.raizlabs.dbflow5.adapter.queriable.SingleModelLoader
import com.raizlabs.dbflow5.adapter.saveable.ModelSaver
import com.raizlabs.dbflow5.annotation.Database
import com.raizlabs.dbflow5.annotation.QueryModel
import com.raizlabs.dbflow5.annotation.Table
import com.raizlabs.dbflow5.database.DatabaseHelperListener
import com.raizlabs.dbflow5.database.DatabaseStatement
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.database.FlowCursor
import com.raizlabs.dbflow5.database.AndroidSQLiteOpenHelper
import com.raizlabs.dbflow5.database.OpenHelper
import com.raizlabs.dbflow5.migration.Migration
import com.raizlabs.dbflow5.runtime.DirectModelNotifier
import com.raizlabs.dbflow5.runtime.ModelNotifier
import com.raizlabs.dbflow5.structure.BaseModelView
import com.raizlabs.dbflow5.transaction.BaseTransactionManager
import com.raizlabs.dbflow5.transaction.DefaultTransactionManager
import com.raizlabs.dbflow5.transaction.DefaultTransactionQueue
import com.raizlabs.dbflow5.transaction.ITransaction
import com.raizlabs.dbflow5.transaction.Transaction

/**
 * Description: The main interface that all Database implementations extend from. This is for internal usage only
 * as it will be generated for every [Database].
 */
abstract class DatabaseDefinition : DatabaseWrapper {

    private val migrationMap = hashMapOf<Int, MutableList<Migration>>()

    private val modelAdapters = hashMapOf<Class<*>, ModelAdapter<*>>()

    private val modelTableNames = hashMapOf<String, Class<*>>()

    private val modelViewAdapterMap = linkedMapOf<Class<*>, ModelViewAdapter<*>>()

    private val queryModelAdapterMap = linkedMapOf<Class<*>, QueryModelAdapter<*>>()

    /**
     * The helper that manages database changes and initialization
     */
    private var openHelper: OpenHelper? = null

    /**
     * Allows for the app to listen for database changes.
     */
    private var helperListener: DatabaseHelperListener? = null

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
        get() = modelAdapters.keys.toList()

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
     * @return The list of [QueryModelAdapter]. Internal method for creating query models in the DB.
     */
    val modelQueryAdapters: List<QueryModelAdapter<*>>
        get() = queryModelAdapterMap.values.toList()

    /**
     * @return The map of migrations to DB version
     */
    val migrations: Map<Int, List<Migration>>
        get() = migrationMap

    val helper: OpenHelper
        @Synchronized get() {
            if (openHelper == null) {
                val config = FlowManager.getConfig().databaseConfigMap[associatedDatabaseClassFile]
                openHelper = if (config?.openHelperCreator != null) {
                    config.openHelperCreator.invoke(this, helperListener)
                } else {
                    AndroidSQLiteOpenHelper(this, helperListener)
                }
                openHelper?.performRestoreFromBackup()
            }
            return openHelper!!
        }

    val writableDatabase: DatabaseWrapper
        get() = helper.database

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
        get() = helper.isDatabaseIntegrityOk

    init {
        @Suppress("LeakingThis")
        applyDatabaseConfig(FlowManager.getConfig().databaseConfigMap[associatedDatabaseClassFile])
    }

    /**
     * Applies a database configuration object to this class.
     */
    @Suppress("UNCHECKED_CAST")
    internal fun applyDatabaseConfig(databaseConfig: DatabaseConfig?) {
        this.databaseConfig = databaseConfig
        if (databaseConfig != null) {
            // initialize configuration if exists.
            val tableConfigCollection = databaseConfig.tableConfigMap.values
            for (tableConfig in tableConfigCollection) {
                val modelAdapter: ModelAdapter<Any> = modelAdapters[tableConfig.tableClass] as ModelAdapter<Any>? ?: continue
                tableConfig.listModelLoader?.let { modelAdapter.listModelLoader = it as ListModelLoader<Any> }
                tableConfig.singleModelLoader?.let { modelAdapter.singleModelLoader = it as SingleModelLoader<Any> }
                tableConfig.modelSaver?.let { modelAdapter.modelSaver = it as ModelSaver<Any> }
            }
            helperListener = databaseConfig.helperListener
        }
        transactionManager = if (databaseConfig?.transactionManagerCreator == null) {
            DefaultTransactionManager(this)
        } else {
            databaseConfig.transactionManagerCreator.invoke(this)
        }
    }

    protected fun <T : Any> addModelAdapter(modelAdapter: ModelAdapter<T>, holder: DatabaseHolder) {
        holder.putDatabaseForTable(modelAdapter.table, this)
        modelTableNames.put(modelAdapter.tableName, modelAdapter.table)
        modelAdapters.put(modelAdapter.table, modelAdapter)
    }

    protected fun <T : Any> addModelViewAdapter(modelViewAdapter: ModelViewAdapter<T>, holder: DatabaseHolder) {
        holder.putDatabaseForTable(modelViewAdapter.table, this)
        modelViewAdapterMap.put(modelViewAdapter.table, modelViewAdapter)
    }

    protected fun <T : Any> addQueryModelAdapter(queryModelAdapter: QueryModelAdapter<T>, holder: DatabaseHolder) {
        holder.putDatabaseForTable(queryModelAdapter.table, this)
        queryModelAdapterMap.put(queryModelAdapter.table, queryModelAdapter)
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
    fun <T : Any> getModelAdapterForTable(table: Class<T>): ModelAdapter<T>? {
        @Suppress("UNCHECKED_CAST")
        return modelAdapters[table] as ModelAdapter<T>?
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
    fun <T : Any> getQueryModelAdapterForQueryClass(queryModel: Class<T>): QueryModelAdapter<T>? =
            queryModelAdapterMap[queryModel] as QueryModelAdapter<T>?

    fun getModelNotifier(): ModelNotifier {
        if (modelNotifier == null) {
            val config = FlowManager.getConfig().databaseConfigMap[associatedDatabaseClassFile]
            modelNotifier = if (config?.modelNotifier == null) {
                DirectModelNotifier()
            } else {
                config.modelNotifier
            }
        }
        return modelNotifier!!
    }

    /**
     * Executes and returns the executed transaction.
     */
    fun <R : Any?> executeTransactionAsync(transaction: ITransaction<R>,
                                           success: ((Transaction<R>, R) -> Unit)? = null,
                                           error: ((Transaction<R>, Throwable) -> Unit)? = null): Transaction<R>
            = beginTransactionAsync(transaction)
            .success(success)
            .error(error)
            .execute()


    fun <R : Any?> beginTransactionAsync(transaction: ITransaction<R>): Transaction.Builder<R> =
            Transaction.Builder(transaction, this)

    fun <R : Any?> beginTransactionAsync(transaction: (DatabaseWrapper) -> R): Transaction.Builder<R> =
            beginTransactionAsync(object : ITransaction<R> {
                override fun execute(databaseWrapper: DatabaseWrapper) = transaction(databaseWrapper)
            })

    fun <R> executeTransaction(transaction: ITransaction<R>): R {
        val database = writableDatabase
        try {
            database.beginTransaction()
            val result = transaction.execute(database)
            database.setTransactionSuccessful()
            return result
        } finally {
            database.endTransaction()
        }
    }

    inline fun <R> executeTransaction(crossinline transaction: (DatabaseWrapper) -> R)
            = executeTransaction(object : ITransaction<R> {
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

    @Deprecated(message = "use {@link #reset()}", replaceWith = ReplaceWith("reset()"))
    fun reset(context: Context) {
        reset(databaseConfig)
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
            applyDatabaseConfig(databaseConfig)
            helper.database
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
            openHelper = null
            applyDatabaseConfig(databaseConfig)
            helper.database
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
            FlowManager.context.deleteDatabase(databaseFileName)
            openHelper = null
            isResetting = false
        }
    }

    /**
     * Closes the DB and stops the [BaseTransactionManager]
     */
    fun close() {
        transactionManager.stopQueue()
        modelAdapters.values.forEach {
            with(it) {
                closeInsertStatement()
                closeCompiledStatement()
                closeDeleteStatement()
                closeUpdateStatement()
            }
        }
        helper.closeDB()
    }

    /**
     * Saves the database as a backup on the [DefaultTransactionQueue]. This will
     * create a THIRD database to use as a backup to the backup in case somehow the overwrite fails.
     *
     * @throws java.lang.IllegalStateException if [Database.backupEnabled]
     * or [Database.consistencyCheckEnabled] is not enabled.
     */
    fun backupDatabase() {
        helper.backupDB()
    }

    override val version: Int
        get() = writableDatabase.version

    override fun execSQL(query: String) = writableDatabase.execSQL(query)

    override fun beginTransaction() = writableDatabase.beginTransaction()

    override fun setTransactionSuccessful() = writableDatabase.setTransactionSuccessful()

    override fun endTransaction() = writableDatabase.endTransaction()

    override fun compileStatement(rawQuery: String): DatabaseStatement
            = writableDatabase.compileStatement(rawQuery)

    override fun rawQuery(query: String, selectionArgs: Array<String>?): FlowCursor
            = writableDatabase.rawQuery(query, selectionArgs)

    override fun updateWithOnConflict(tableName: String,
                                      contentValues: ContentValues,
                                      where: String?,
                                      whereArgs: Array<String>?,
                                      conflictAlgorithm: Int): Long
            = writableDatabase.updateWithOnConflict(tableName, contentValues, where, whereArgs, conflictAlgorithm)

    override fun insertWithOnConflict(
            tableName: String,
            nullColumnHack: String?,
            values: ContentValues,
            sqLiteDatabaseAlgorithmInt: Int): Long
            = writableDatabase.insertWithOnConflict(tableName, nullColumnHack, values, sqLiteDatabaseAlgorithmInt)

    override fun query(
            tableName: String,
            columns: Array<String>?,
            selection: String?,
            selectionArgs: Array<String>?,
            groupBy: String?,
            having: String?,
            orderBy: String?): FlowCursor
            = writableDatabase.query(tableName, columns, selection, selectionArgs, groupBy, having, orderBy)

    override fun delete(tableName: String, whereClause: String?, whereArgs: Array<String>?): Int
            = writableDatabase.delete(tableName, whereClause, whereArgs)
}
