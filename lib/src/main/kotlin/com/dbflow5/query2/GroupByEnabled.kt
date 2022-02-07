package com.dbflow5.query2

import com.dbflow5.query.NameAlias
import com.dbflow5.query.property.IProperty

interface GroupByEnabled<
    Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>> {

    infix fun groupBy(nameAlias: NameAlias): WhereWithGroupBy<Table, Result, OperationBase>
    fun groupBy(vararg nameAliases: NameAlias): WhereWithGroupBy<Table, Result, OperationBase>
    infix fun groupBy(property: IProperty<*>): WhereWithGroupBy<Table, Result, OperationBase>
    fun groupBy(vararg properties: IProperty<*>): WhereWithGroupBy<Table, Result, OperationBase>
}