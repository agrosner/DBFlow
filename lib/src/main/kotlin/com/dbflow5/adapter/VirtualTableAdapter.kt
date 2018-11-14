package com.dbflow5.adapter

import com.dbflow5.config.DBFlowDatabase

/**
 * Description:
 */
abstract class VirtualTableAdapter<T : Any>(db: DBFlowDatabase) : RetrievalAdapter<T>(db) {

    /**
     * @return The query used to create this table.
     */
    abstract val creationQuery: String
}