package com.dbflow5.query

import com.dbflow5.adapter.DBRepresentable
import com.dbflow5.query.operations.Property

interface GroupByEnabled<
    Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>,
    Representable : DBRepresentable<Table>> {

    infix fun groupBy(nameAlias: NameAlias): WhereWithGroupBy<Table, Result, OperationBase, Representable>
    fun groupBy(vararg nameAliases: NameAlias): WhereWithGroupBy<Table, Result, OperationBase, Representable>
    infix fun groupBy(property: Property<*, Table>): WhereWithGroupBy<Table, Result, OperationBase, Representable>
    fun groupBy(vararg properties: Property<*, Table>): WhereWithGroupBy<Table, Result, OperationBase, Representable>
}