package com.dbflow5.query2

import com.dbflow5.query.SQLOperator

interface HavingEnabled<
    Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>> {

    infix fun having(operator: SQLOperator): WhereWithHaving<Table, Result, OperationBase>
    fun having(vararg operators: SQLOperator): WhereWithHaving<Table, Result, OperationBase>
}
