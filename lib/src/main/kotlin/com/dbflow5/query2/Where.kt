package com.dbflow5.query2

import com.dbflow5.adapter.RetrievalAdapter
import com.dbflow5.query.SQLOperator
import com.dbflow5.sql.Query

/**
 * Description:
 */
interface Where<Table : Any, OperationBase> : Query, HasAdapter<Table,
    RetrievalAdapter<Table>> {

}

internal fun <Table : Any, OperationBase> RetrievalAdapter<Table>.where(
    queryBase: Query,
    operator: SQLOperator,
): Where<Table, OperationBase> = WhereImpl(adapter = this)

internal class WhereImpl<Table : Any, OperationBase>(
    override val adapter: RetrievalAdapter<Table>,
) : Where<Table, OperationBase> {
    override val query: String
        get() = TODO("Not yet implemented")
}