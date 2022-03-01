package com.dbflow5.query

interface Offsettable<
    Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>> {
    infix fun offset(offset: Long): WhereWithOffset<Table, Result, OperationBase>
}
