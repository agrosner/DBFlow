package com.dbflow5.query2

import com.dbflow5.query.NameAlias
import com.dbflow5.query2.operations.Property

interface GroupByEnabled<
    Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>> {

    infix fun groupBy(nameAlias: NameAlias): WhereWithGroupBy<Table, Result, OperationBase>
    fun groupBy(vararg nameAliases: NameAlias): WhereWithGroupBy<Table, Result, OperationBase>
    infix fun groupBy(property: Property<*, Table>): WhereWithGroupBy<Table, Result, OperationBase>
    fun groupBy(vararg properties: Property<*, Table>): WhereWithGroupBy<Table, Result, OperationBase>
}