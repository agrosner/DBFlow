package com.dbflow5.query

import com.dbflow5.adapter.QueryRepresentable

/**
 * Description:
 */
interface HasAdapter<Table : Any, Adapter : QueryRepresentable<Table>> {
    val adapter: Adapter
}