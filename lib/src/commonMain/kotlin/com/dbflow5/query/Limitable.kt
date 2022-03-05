package com.dbflow5.query

import com.dbflow5.adapter.DBRepresentable

interface Limitable<
    Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>,
    Representable : DBRepresentable<Table>> {

    infix fun limit(count: Long): WhereWithLimit<Table, Result, OperationBase, Representable>
}
