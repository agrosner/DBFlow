package com.dbflow5.query2

import com.dbflow5.adapter.SQLObjectAdapter
import com.dbflow5.sql.Query

interface Delete<Table : Any> : Query,
    HasAdapter<Table, SQLObjectAdapter<Table>>,
    Whereable<Table, Delete<Table>, SQLObjectAdapter<Table>>, Indexable<Table>

fun <Table : Any> SQLObjectAdapter<Table>.delete(): Delete<Table> = DeleteImpl(adapter = this)

internal class DeleteImpl<Table : Any>(
    override val adapter: SQLObjectAdapter<Table>
) : Delete<Table> {

    override val query: String by lazy {
        buildString {
            append("DELETE FROM ${adapter.name} ")
        }
    }
}