package com.dbflow5.query

import com.dbflow5.adapter2.QueryRepresentable

/**
 * Description:
 */
interface HasAdapter<Table : Any, Adapter : QueryRepresentable<Table>> {
    val adapter: Adapter
}