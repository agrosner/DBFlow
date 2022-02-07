package com.dbflow5.query2

interface Offsettable<
    Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>> {
    infix fun offset(offset: Long): WhereWithOffset<Table, Result, OperationBase>
}
