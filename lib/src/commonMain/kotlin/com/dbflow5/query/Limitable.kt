package com.dbflow5.query

interface Limitable<
    Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>> {

    infix fun limit(count: Long): WhereWithLimit<Table, Result, OperationBase>
}
