package com.dbflow5.query

import com.dbflow5.query.operations.AnyOperator

interface HavingEnabled<
    Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>> {

    infix fun having(operator: AnyOperator): WhereWithHaving<Table, Result, OperationBase>
    fun having(vararg operators: AnyOperator): WhereWithHaving<Table, Result, OperationBase>
}
