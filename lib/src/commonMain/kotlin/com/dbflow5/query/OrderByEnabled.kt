package com.dbflow5.query

import com.dbflow5.query.operations.Property

interface OrderByEnabled<
    Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>> {

    infix fun orderBy(orderBy: OrderBy): WhereWithOrderBy<Table, Result, OperationBase>
    fun orderBy(vararg orderBies: OrderBy): WhereWithOrderBy<Table, Result, OperationBase>

    fun orderBy(
        nameAlias: NameAlias,
        ascending: Boolean = true
    ): WhereWithOrderBy<Table, Result, OperationBase>

    fun orderBy(
        property: Property<*, Table>,
        ascending: Boolean = true
    ): WhereWithOrderBy<Table, Result, OperationBase>

    infix fun orderByAll(
        orderByList: List<OrderBy>
    ): WhereWithOrderBy<Table, Result, OperationBase>
}
