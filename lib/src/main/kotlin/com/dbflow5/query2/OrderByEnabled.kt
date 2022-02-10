package com.dbflow5.query2

import com.dbflow5.query.NameAlias
import com.dbflow5.query.OrderBy
import com.dbflow5.query.property.IProperty
import com.dbflow5.query2.operations.Property

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
        property: Property<out Any, Table>,
        ascending: Boolean = true
    ): WhereWithOrderBy<Table, Result, OperationBase>

    infix fun orderByAll(
        orderByList: List<OrderBy>
    ): WhereWithOrderBy<Table, Result, OperationBase>
}
