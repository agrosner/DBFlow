package com.dbflow5.query

import com.dbflow5.adapter.DBRepresentable
import com.dbflow5.sql.Query

interface Delete<Table : Any> : Query,
    HasAdapter<Table, DBRepresentable<Table>>,
    Whereable<Table, Long, Delete<Table>>,
    Indexable<Table>

fun <Table : Any> DBRepresentable<Table>.delete(): Delete<Table> = DeleteImpl(adapter = this)

internal class DeleteImpl<Table : Any>(
    override val adapter: DBRepresentable<Table>,
    override val resultFactory: ResultFactory<Long> = UpdateDeleteResultFactory(
        adapter,
        isDelete = true
    ),
) : Delete<Table> {

    override val associatedAdapters: List<DBRepresentable<*>> = listOf(adapter)

    override val query: String by lazy {
        buildString {
            append("DELETE FROM ${adapter.name} ")
        }
    }
}
