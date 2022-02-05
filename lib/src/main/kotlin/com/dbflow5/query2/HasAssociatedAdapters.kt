package com.dbflow5.query2

import com.dbflow5.adapter.RetrievalAdapter

/**
 * Description: Keeps track of associated adapters in the query
 * for registration on table observing.
 */
interface HasAssociatedAdapters {

    /**
     * The list of tables referenced in this query.
     */
    val associatedAdapters: List<RetrievalAdapter<*>>
}