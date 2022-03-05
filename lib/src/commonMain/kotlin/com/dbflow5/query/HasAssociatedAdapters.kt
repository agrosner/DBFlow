package com.dbflow5.query

import com.dbflow5.adapter.DBRepresentable

/**
 * Description: Keeps track of associated adapters in the query
 * for registration on table observing.
 */
interface HasAssociatedAdapters<Representable : DBRepresentable<out Any>> {

    /**
     * The list of tables referenced in this query.
     */
    val associatedAdapters: List<Representable>
}