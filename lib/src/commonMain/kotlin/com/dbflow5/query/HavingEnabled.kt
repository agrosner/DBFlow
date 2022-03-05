package com.dbflow5.query

import com.dbflow5.adapter.DBRepresentable
import com.dbflow5.query.operations.AnyOperator

interface HavingEnabled<
    Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>,
    Representable : DBRepresentable<Table>> {

    infix fun having(operator: AnyOperator): WhereWithHaving<Table, Result, OperationBase, Representable>
    fun having(vararg operators: AnyOperator): WhereWithHaving<Table, Result, OperationBase, Representable>
}
