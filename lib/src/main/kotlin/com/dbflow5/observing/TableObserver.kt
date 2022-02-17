package com.dbflow5.observing

import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.config.FlowLog
import com.dbflow5.config.FlowManager
import com.dbflow5.config.beginTransactionAsync
import com.dbflow5.database.DatabaseStatement
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.database.SQLiteException
import com.dbflow5.database.executeTransaction
import com.dbflow5.query.TriggerMethod
import com.dbflow5.quoteIfNeeded
import com.dbflow5.stripQuotes
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass

/**
 * Description: Tracks table changes in the DB via Triggers. This more efficient than utilizing
 * in the app space.
 */
class TableObserver<DB : DBFlowDatabase> internal constructor(
    private val db: DB,
    private val tables: List<KClass<*>>
) {

    private val tableReferenceMap = hashMapOf<KClass<*>, Int>()
    private val tableIndexToNameMap = hashMapOf<Int, KClass<*>>()

    private val observingTableTracker = ObservingTableTracker(tableCount = tables.size)
    private val observerToObserverWithIdsMap =
        mutableMapOf<OnTableChangedObserver, OnTableChangedObserverWithIds>()

    private val tableStatus = BooleanArray(tables.size)

    private var initialized = false

    private val pendingRefresh = AtomicBoolean(false)

    private val cleanupStatement: DatabaseStatement by lazy {
        db.compileStatement(
            "UPDATE $TABLE_OBSERVER_NAME SET $INVALIDATED_COLUMN_NAME = 0 WHERE $INVALIDATED_COLUMN_NAME = 1;"
        )
    }

    init {
        tables.withIndex().forEach { (index, name) ->
            tableReferenceMap[name] = index
            tableIndexToNameMap[index] = name
        }

    }

    fun addOnTableChangedObserver(observer: OnTableChangedObserver) {
        val newTableIds = IntArray(observer.tables.size)
        observer.tables.forEachIndexed { index, table ->
            val id = tableReferenceMap[table]
            newTableIds[index] = id ?: throw IllegalArgumentException("No Table found for $table")
        }
        val wrapped = OnTableChangedObserverWithIds(observer, newTableIds)
        synchronized(observerToObserverWithIdsMap) {
            if (!observerToObserverWithIdsMap.containsKey(observer)) {
                observerToObserverWithIdsMap[observer] = wrapped

                if (observingTableTracker.onAdded(newTableIds)) {
                    syncTriggers()
                }
            }
        }
    }

    fun removeOnTableChangedObserver(observer: OnTableChangedObserver) {
        synchronized(observerToObserverWithIdsMap) {
            observerToObserverWithIdsMap.remove(observer)?.let { observer ->
                if (observingTableTracker.onRemoved(observer.tableIds)) {
                    syncTriggers()
                }
            }
        }
    }

    /**
     * Enqueues a table update check on the [DBFlowDatabase] Transaction queue.
     */
    fun enqueueTableUpdateCheck() {
        if (!pendingRefresh.compareAndSet(false, true)) {
            db.beginTransactionAsync { checkForTableUpdates(db) }
                .shouldRunInTransaction(false)
                .enqueue(error = { _, e ->
                    FlowLog.log(FlowLog.Level.E, "Could not check for table updates", e)
                })
        }
    }

    fun checkForTableUpdates() {
        syncTriggers()
        checkForTableUpdates(db)
    }


    internal fun construct(db: DatabaseWrapper) {
        synchronized(this) {
            if (initialized) {
                FlowLog.log(FlowLog.Level.W, "TableObserver already initialized")
                return
            }
            db.execSQL("PRAGMA temp_store = MEMORY;")
            db.executeTransaction {
                db.execSQL("PRAGMA recursive_triggers='ON';")
                db.execSQL("CREATE TEMP TABLE $TABLE_OBSERVER_NAME($TABLE_ID_COLUMN_NAME INTEGER PRIMARY KEY, $INVALIDATED_COLUMN_NAME INTEGER NOT NULL DEFAULT 0);")
            }
            syncTriggers(db)
            initialized = true
        }
    }

    internal fun syncTriggers() {
        if (db.isOpened) {
            syncTriggers(db)
        }
    }

    internal fun syncTriggers(db: DatabaseWrapper) {
        if (db.isInTransaction) {
            // don't run in another transaction.
            return
        }

        try {
            while (true) {
                val lock = this.db.closeLock
                lock.lock()
                try {
                    val tablesToSync = observingTableTracker.tablesToSync ?: return
                    db.executeTransaction {
                        tablesToSync.forEachIndexed { index, operation ->
                            // return value of Unit to make sure exhaustive "when".
                            @Suppress("UNUSED_VARIABLE")
                            val exhaustive = when (operation) {
                                ObservingTableTracker.Operation.Add -> observeTable(db, index)
                                ObservingTableTracker.Operation.Remove -> stopObservingTable(
                                    db,
                                    index
                                )
                                ObservingTableTracker.Operation.None -> {
                                    // don't do anything
                                }
                            }
                        }
                    }
                    observingTableTracker.syncCompleted()
                } finally {
                    lock.unlock()
                }
            }
        } catch (e: Exception) {
            when (e) {
                is IllegalStateException, is SQLiteException -> {
                    FlowLog.log(FlowLog.Level.E, "Cannot sync table TRIGGERs. Is the db closed?", e)
                }
                else -> throw e
            }
        }
    }

    internal fun checkForTableUpdates(db: DB) {
        val lock = db.closeLock
        var hasUpdatedTable = false

        try {
            lock.lock()

            if (!db.isOpened) {
                return
            }

            if (!initialized) {
                db.openHelper.database
            }
            if (!initialized) {
                FlowLog.log(
                    FlowLog.Level.E,
                    "Database is not initialized even though open. Is this an error?"
                )
                return
            }

            if (!pendingRefresh.compareAndSet(true, false)) {
                FlowLog.log(FlowLog.Level.W, "TableObserver pending refresh had completed.")
            }

            if (db.isInTransaction) {
                return
            }

            hasUpdatedTable = checkUpdatedTables()

        } catch (e: Exception) {
            when (e) {
                is IllegalStateException, is SQLiteException -> {
                    FlowLog.log(
                        FlowLog.Level.E,
                        "Cannot check for table updates. is the db closed?",
                        e
                    )
                }
                else -> throw e
            }
        } finally {
            lock.unlock()
        }
        if (hasUpdatedTable) {
            synchronized(observerToObserverWithIdsMap) {
                observerToObserverWithIdsMap.forEach { (_, observer) ->
                    observer.notifyTables(tableStatus)
                }
            }
            // reset
            tableStatus.forEachIndexed { index, _ ->
                tableStatus[index] = false
            }
        }
    }

    private fun checkUpdatedTables(): Boolean {
        var hasUpdatedTable = false
        db.rawQuery(SELECT_UPDATED_TABLES_SQL, null).use { cursor ->
            while (cursor.moveToNext()) {
                val tableId = cursor.getInt(0)
                tableStatus[tableId] = true
                hasUpdatedTable = true
            }
        }
        if (hasUpdatedTable) {
            cleanupStatement.executeUpdateDelete()
        }
        return hasUpdatedTable
    }

    private fun observeTable(db: DatabaseWrapper, tableId: Int) {
        db.execSQL("INSERT OR IGNORE INTO $TABLE_OBSERVER_NAME VALUES($tableId, 0)")
        val tableName = tables[tableId]

        TriggerMethod.All.forEach { method ->
            // utilize raw query, since we're using dynamic tables not supported by query language.
            db.execSQL(
                "CREATE TEMP TRIGGER IF NOT EXISTS ${getTriggerName(tableName, method.value)} " +
                    "AFTER $method ON ${
                        FlowManager.getTableName(tableName).quoteIfNeeded()
                    } BEGIN UPDATE $TABLE_OBSERVER_NAME " +
                    "SET $INVALIDATED_COLUMN_NAME = 1 " +
                    "WHERE $TABLE_ID_COLUMN_NAME = $tableId " +
                    "AND $INVALIDATED_COLUMN_NAME = 0; END"
            )
        }
    }

    private fun stopObservingTable(db: DatabaseWrapper, tableId: Int) {
        val tableName = tables[tableId]
        TriggerMethod.All.forEach { method ->
            db.execSQL("DROP TRIGGER IF EXISTS ${getTriggerName(tableName, method.value)}")
        }
    }

    private fun getTriggerName(table: KClass<*>, method: String) =
        "`${TRIGGER_PREFIX}_${FlowManager.getTableName(table).stripQuotes()}_$method`"

    companion object {

        private const val TABLE_OBSERVER_NAME = "dbflow_table_log"
        private const val TRIGGER_PREFIX = "dbflow_table_trigger"
        private const val INVALIDATED_COLUMN_NAME = "invalidated"
        private const val TABLE_ID_COLUMN_NAME = "table_id"
        private const val SELECT_UPDATED_TABLES_SQL =
            "SELECT * FROM $TABLE_OBSERVER_NAME WHERE $INVALIDATED_COLUMN_NAME = 1;"
    }
}