package com.dbflow5.query

import com.dbflow5.adapter.DBRepresentable

interface Constrainable<
    Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>,
    Representable : DBRepresentable<Table>> {

    fun constrain(offset: Long, limit: Long): WhereWithOffset<Table, Result, OperationBase, Representable>
}