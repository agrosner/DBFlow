package com.dbflow5.database

import com.dbflow5.config.DBFlowDatabase

/**
 * Description: Tracks table changes in the DB via Triggers. This more efficient than utilizing
 * in the app space.
 */
class TableObserver internal constructor(private val db: DBFlowDatabase, tableNames: List<String>) {

    private val tableReferenceMap = hashMapOf<String, Int>()
    private val tableIndexToNameMap = hashMapOf<Int, String>()

    init {
        tableNames.withIndex().forEach { (index, name) ->
            tableReferenceMap[name] = index
            tableIndexToNameMap[index] = name
        }

    }

    private fun observeTable(db: DBFlowDatabase, tableId: Int) {
        db.execSQL("INSERT OR IGNORE INTO $TABLE_OBSERVER_NAME VALUES($tableId, 0)")

    }


    companion object {

        private const val TABLE_OBSERVER_NAME = "dbflow_table_log"

    }
}