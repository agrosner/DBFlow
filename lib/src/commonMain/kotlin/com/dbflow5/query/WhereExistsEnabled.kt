package com.dbflow5.query

import com.dbflow5.adapter.DBRepresentable

interface WhereExistsEnabled<
    Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>,
    Representable : DBRepresentable<Table>> {

    infix fun <OtherTable : Any, OtherOperationBase : ExecutableQuery<Result>> whereExists(
        whereable: Where<OtherTable, Result, OtherOperationBase, *>
    ): WhereExists<Table, Result, OperationBase, Representable>

    fun <OtherTable : Any, OtherOperationBase : ExecutableQuery<Result>> whereExists(
        not: Boolean = false,
        whereable: Where<OtherTable, Result, OtherOperationBase, *>
    ): WhereExists<Table, Result, OperationBase, Representable>
}
