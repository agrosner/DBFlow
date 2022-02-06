package com.dbflow5.query2

import com.dbflow5.query.NameAlias
import com.dbflow5.query.property.IProperty

/**
 * Description:
 */
interface GroupByEnabled<Table : Any, OperationBase> {

    infix fun groupBy(nameAlias: NameAlias): WhereWithGroupBy<Table, OperationBase>
    fun groupBy(vararg nameAliases: NameAlias): WhereWithGroupBy<Table, OperationBase>
    infix fun groupBy(property: IProperty<*>): WhereWithGroupBy<Table, OperationBase>
    fun groupBy(vararg properties: IProperty<*>): WhereWithGroupBy<Table, OperationBase>
}