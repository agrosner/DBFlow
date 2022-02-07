package com.dbflow5.query2

interface Constrainable<
    Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>> {

    fun constrain(offset: Long, limit: Long): WhereWithOffset<Table, Result, OperationBase>
}