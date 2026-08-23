package com.dbflow5.query

import com.dbflow5.adapter.DBRepresentable
import com.dbflow5.adapter.WritableDBRepresentable
import com.dbflow5.sql.Query

interface Delete<Table : Any> : Query,
    HasAdapter<Table, WritableDBRepresentable<Table>>,
    Whereable<Table, Long, Delete<Table>, WritableDBRepresentable<Table>>,
    Indexable<Table>

fun <Table : Any> WritableDBRepresentable<Table>.delete(): Delete<Table> = DeleteImpl(adapter = this)

internal class DeleteImpl<Table : Any>(
    override val adapter: WritableDBRepresentable<Table>,
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
