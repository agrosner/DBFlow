package com.dbflow5.query

import com.dbflow5.adapter.DBRepresentable
import com.dbflow5.query.operations.Property

interface OrderByEnabled<
    Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>,
    Representable : DBRepresentable<Table>> {

    infix fun orderBy(orderBy: OrderBy): WhereWithOrderBy<Table, Result, OperationBase, Representable>
    fun orderBy(vararg orderBies: OrderBy): WhereWithOrderBy<Table, Result, OperationBase, Representable>

    fun orderBy(
        nameAlias: NameAlias,
        ascending: Boolean = true
    ): WhereWithOrderBy<Table, Result, OperationBase, Representable>

    fun orderBy(
        property: Property<*, Table>,
        ascending: Boolean = true
    ): WhereWithOrderBy<Table, Result, OperationBase, Representable>

    infix fun orderByAll(
        orderByList: List<OrderBy>
    ): WhereWithOrderBy<Table, Result, OperationBase, Representable>
}
