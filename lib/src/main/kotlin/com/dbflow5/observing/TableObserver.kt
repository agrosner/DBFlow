package com.dbflow5.observing

import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.config.FlowLog
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.database.SQLiteException
import com.dbflow5.database.executeTransaction
import com.dbflow5.query.TriggerMethod

/**
 * Description: Tracks table changes in the DB via Triggers. This more efficient than utilizing
 * in the app space.
 */
class TableObserver internal constructor(private val db: DBFlowDatabase,
                                         private val tableNames: List<String>) {

    private val tableReferenceMap = hashMapOf<String, Int>()
    private val tableIndexToNameMap = hashMapOf<Int, String>()

    private var initialized = false

    init {
        tableNames.withIndex().forEach { (index, name) ->
            tableReferenceMap[name] = index
            tableIndexToNameMap[index] = name
        }

    }

    internal fun construct(db: DatabaseWrapper) {
        synchronized(this) {
            if (initialized) {
                FlowLog.log(FlowLog.Level.W, "TableObserver already initialized")
                return
            }

            db.executeTransaction {
                db.execSQL("PRAGMA temp_store = MEMORY;")
                db.execSQL("PRAGMA recursive_triggers='ON';")
            }
            syncTriggers(db)
            initialized = true
        }
    }

    internal fun syncTriggers(db: DatabaseWrapper) {
        if (db.isInTransaction) {
            // don't run in another transaction.
            return
        }

        try {
            while (true) {

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

    private fun observeTable(db: DatabaseWrapper, tableId: Int) {
        db.execSQL("INSERT OR IGNORE INTO $TABLE_OBSERVER_NAME VALUES($tableId, 0)")
        val tableName = tableNames[tableId]

        TriggerMethod.METHODS.forEach { method ->
            // utilize raw query, since we're using dynamic tables not supported by query language.
            db.execSQL("CREATE TEMP TRIGGER IF NOT EXISTS ${getTriggerName(tableName, method)} " +
                    "AFTER $method ON `$tableName` BEGIN UPDATE $TABLE_OBSERVER_NAME " +
                    "SET $INVALIDATED_COLUMN_NAME = 1 " +
                    "WHERE $TABLE_ID_COLUMN_NAME = $tableId " +
                    "AND $INVALIDATED_COLUMN_NAME = 0; END")
        }
    }

    private fun stopObservingTable(db: DatabaseWrapper, tableId: Int) {
        val tableName = tableNames[tableId]
        TriggerMethod.METHODS.forEach { method ->
            db.execSQL("DROP TRIGGER IF EXISTS ${getTriggerName(tableName, method)}")
        }
    }

    private fun getTriggerName(tableName: String, method: String) =
            "`${TRIGGER_PREFIX}_${tableName}_$method`"

    companion object {

        private const val TABLE_OBSERVER_NAME = "dbflow_table_log"
        private const val TRIGGER_PREFIX = "dbflow_table_trigger"
        private const val INVALIDATED_COLUMN_NAME = "invalidated"
        private const val TABLE_ID_COLUMN_NAME = "table_id"
    }
}