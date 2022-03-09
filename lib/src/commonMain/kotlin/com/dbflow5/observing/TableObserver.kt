package com.dbflow5.observing

import com.dbflow5.adapter.DBRepresentable
import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.config.FlowLog
import com.dbflow5.config.beginTransactionAsync
import com.dbflow5.database.DatabaseStatement
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.database.SQLiteException
import com.dbflow5.mpp.runBlocking
import com.dbflow5.mpp.use
import com.dbflow5.query.TriggerMethod
import com.dbflow5.quoteIfNeeded
import com.dbflow5.stripQuotes
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.atomicfu.locks.withLock

/**
 * Description: Tracks table changes in the DB via Triggers. This more efficient than utilizing
 * in the app space.
 */
class TableObserver<DB : DBFlowDatabase<DB>> internal constructor(
    private val db: DB,
    private val adapters: List<DBRepresentable<*>>
) : SynchronizedObject() {

    private val tableReferenceMap = hashMapOf<DBRepresentable<*>, Int>()
    private val tableIndexToNameMap = hashMapOf<Int, DBRepresentable<*>>()

    private val observingTableTracker = ObservingTableTracker(tableCount = adapters.size)
    private val observerToObserverWithIdsMap = SynchronizedTableObserverMap()

    private val tableStatus = BooleanArray(adapters.size)

    private var initialized = false

    private val pendingRefresh = atomic(false)

    private val cleanupStatement: DatabaseStatement by lazy {
        db.compileStatement(
            "UPDATE $TABLE_OBSERVER_NAME SET $INVALIDATED_COLUMN_NAME = 0 WHERE $INVALIDATED_COLUMN_NAME = 1;"
        )
    }

    init {
        adapters.withIndex().forEach { (index, name) ->
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
        if (!pendingRefresh.compareAndSet(expect = false, update = true)) {
            db.beginTransactionAsync { checkForTableUpdates(db) }
                .shouldRunInTransaction(false)
                .enqueue(error = { _, e ->
                    FlowLog.log(FlowLog.Level.E, "Could not check for table updates", throwable = e)
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
                return@synchronized
            }
            db.execSQL("PRAGMA temp_store = MEMORY;")
            runBlocking {
                db.executeTransaction {
                    db.execSQL("PRAGMA recursive_triggers='ON';")
                    db.execSQL("CREATE TEMP TABLE $TABLE_OBSERVER_NAME($TABLE_ID_COLUMN_NAME INTEGER PRIMARY KEY, $INVALIDATED_COLUMN_NAME INTEGER NOT NULL DEFAULT 0);")
                }
            }
            syncTriggers(db)
            initialized = true
        }
    }

    internal fun syncTriggers() {
        if (db.isOpen) {
            syncTriggers(db)
        }
    }

    internal fun syncTriggers(db: DatabaseWrapper) {
        if (db.isInTransaction) {
            // don't run in another transaction.
            return
        }

        try {
            this@TableObserver.db.closeLock.withLock {
                val tablesToSync = observingTableTracker.tablesToSync ?: return@withLock
                runBlocking {
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
                }
                observingTableTracker.syncCompleted()
            }
        } catch (e: Exception) {
            when (e) {
                is IllegalStateException, is SQLiteException -> {
                    FlowLog.logError(e, "Cannot sync table TRIGGERs. Is the db closed?")
                }
                else -> throw e
            }
        }
    }

    internal fun checkForTableUpdates(db: DB) {
        val lock = db.closeLock
        var hasUpdatedTable = false

        try {
            runBlocking {
                lock.withLock {
                    if (!db.isOpen) {
                        return@withLock
                    }

                    if (!initialized) {
                        db.openHelper.database
                    }
                    if (!initialized) {
                        FlowLog.log(
                            FlowLog.Level.E,
                            "Database is not initialized even though open. Is this an error?"
                        )
                        return@withLock
                    }

                    if (!pendingRefresh.compareAndSet(expect = true, update = false)) {
                        FlowLog.log(FlowLog.Level.W, "TableObserver pending refresh had completed.")
                    }

                    if (db.isInTransaction) {
                        return@withLock
                    }

                    hasUpdatedTable = checkUpdatedTables()
                }
            }
        } catch (e: Exception) {
            when (e) {
                is IllegalStateException, is SQLiteException -> {
                    FlowLog.logError(
                        e,
                        "Cannot check for table updates. is the db closed?",
                    )
                }
                else -> throw e
            }
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
        val adapter = adapters[tableId]

        TriggerMethod.All.forEach { method ->
            // utilize raw query, since we're using dynamic tables not supported by query language.
            db.execSQL(
                "CREATE TEMP TRIGGER IF NOT EXISTS ${getTriggerName(adapter, method.value)} " +
                    "AFTER ${method.value} ON ${adapter.name.quoteIfNeeded()} " +
                    "BEGIN UPDATE $TABLE_OBSERVER_NAME " +
                    "SET $INVALIDATED_COLUMN_NAME = 1 " +
                    "WHERE $TABLE_ID_COLUMN_NAME = $tableId " +
                    "AND $INVALIDATED_COLUMN_NAME = 0; END"
            )
        }
    }

    private fun stopObservingTable(db: DatabaseWrapper, tableId: Int) {
        val adapter = adapters[tableId]
        TriggerMethod.All.forEach { method ->
            db.execSQL("DROP TRIGGER IF EXISTS ${getTriggerName(adapter, method.value)}")
        }
    }

    private fun getTriggerName(adapter: DBRepresentable<*>, method: String) =
        "`${TRIGGER_PREFIX}_${adapter.name.stripQuotes()}_$method`"

    companion object {

        private const val TABLE_OBSERVER_NAME = "dbflow_table_log"
        private const val TRIGGER_PREFIX = "dbflow_table_trigger"
        private const val INVALIDATED_COLUMN_NAME = "invalidated"
        private const val TABLE_ID_COLUMN_NAME = "table_id"
        private const val SELECT_UPDATED_TABLES_SQL =
            "SELECT * FROM $TABLE_OBSERVER_NAME WHERE $INVALIDATED_COLUMN_NAME = 1;"
    }
}

private class SynchronizedTableObserverMap(
    private val map: MutableMap<OnTableChangedObserver, OnTableChangedObserverWithIds>
    = mutableMapOf()
) : MutableMap<OnTableChangedObserver, OnTableChangedObserverWithIds> by map, SynchronizedObject()
