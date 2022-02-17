package com.dbflow5.query

import com.dbflow5.adapter.RetrievalAdapter

/**
 * Description:
 */
interface HasAdapter<Table : Any, Adapter : RetrievalAdapter<Table>> {
    val adapter: Adapter
}