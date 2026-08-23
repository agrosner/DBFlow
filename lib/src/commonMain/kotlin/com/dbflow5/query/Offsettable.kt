package com.dbflow5.query

import com.dbflow5.adapter.DBRepresentable

interface Offsettable<
    Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>,
    Representable : DBRepresentable<Table>> {
    infix fun offset(offset: Long): WhereWithOffset<Table, Result, OperationBase, Representable>
}
