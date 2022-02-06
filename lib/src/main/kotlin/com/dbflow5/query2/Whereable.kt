package com.dbflow5.query2

import com.dbflow5.adapter.RetrievalAdapter
import com.dbflow5.query.NameAlias
import com.dbflow5.query.OrderBy
import com.dbflow5.query.SQLOperator
import com.dbflow5.query.property.IProperty
import com.dbflow5.sql.Query

/**
 * Description:
 */
interface Whereable<Table : Any,
    OperationBase,
    Adapter : RetrievalAdapter<Table>> :
    HasAdapter<Table, Adapter>,
    GroupByEnabled<Table, OperationBase>,
    HavingEnabled<Table, OperationBase>,
    Limitable<Table, OperationBase>,
    Offsettable<Table, OperationBase>,
    OrderByEnabled<Table, OperationBase>,
    WhereExistsEnabled<Table, OperationBase>,
    Constrainable<Table, OperationBase>,
    Query {

    infix fun where(operator: SQLOperator): WhereStart<Table, OperationBase> =
        adapter.where(this, operator)

    fun where(vararg operators: SQLOperator): WhereStart<Table, OperationBase> =
        adapter.where(this, *operators)

    override fun groupBy(nameAlias: NameAlias): WhereWithGroupBy<Table, OperationBase> =
        adapter.where<Table, OperationBase>(this).groupBy(nameAlias)

    override fun groupBy(vararg nameAliases: NameAlias): WhereWithGroupBy<Table, OperationBase> =
        adapter.where<Table, OperationBase>(this).groupBy(*nameAliases)

    override fun groupBy(property: IProperty<*>): WhereWithGroupBy<Table, OperationBase> =
        adapter.where<Table, OperationBase>(this).groupBy(property)

    override fun groupBy(vararg properties: IProperty<*>): WhereWithGroupBy<Table, OperationBase> =
        adapter.where<Table, OperationBase>(this).groupBy(*properties)

    override fun having(operator: SQLOperator): WhereWithHaving<Table, OperationBase> =
        adapter.where<Table, OperationBase>(this).having(operator)

    override fun having(vararg operators: SQLOperator): WhereWithHaving<Table, OperationBase> =
        adapter.where<Table, OperationBase>(this).having(*operators)

    override fun limit(count: Long): WhereWithLimit<Table, OperationBase> =
        adapter.where<Table, OperationBase>(this).limit(count)

    override fun offset(offset: Long): WhereWithOffset<Table, OperationBase> =
        adapter.where<Table, OperationBase>(this).offset(offset)

    override fun orderBy(orderBy: OrderBy): WhereWithOrderBy<Table, OperationBase> =
        adapter.where<Table, OperationBase>(this).orderBy(orderBy)

    override fun orderBy(vararg orderBies: OrderBy): WhereWithOrderBy<Table, OperationBase> =
        adapter.where<Table, OperationBase>(this).orderBy(*orderBies)

    override fun orderBy(
        nameAlias: NameAlias,
        ascending: Boolean
    ): WhereWithOrderBy<Table, OperationBase> =
        adapter.where<Table, OperationBase>(this).orderBy(nameAlias, ascending)

    override fun orderBy(
        property: IProperty<*>,
        ascending: Boolean
    ): WhereWithOrderBy<Table, OperationBase> =
        adapter.where<Table, OperationBase>(this).orderBy(property, ascending)

    override fun orderByAll(orderByList: List<OrderBy>): WhereWithOrderBy<Table, OperationBase> =
        adapter.where<Table, OperationBase>(this).orderByAll(orderByList)

    override fun <OtherTable : Any, OtherOperationBase> whereExists(whereable: Where<OtherTable, OtherOperationBase>): WhereExists<Table, OperationBase> =
        adapter.where<Table, OperationBase>(this).whereExists(whereable)

    override fun <OtherTable : Any, OtherOperationBase> whereExists(
        not: Boolean,
        whereable: Where<OtherTable, OtherOperationBase>
    ): WhereExists<Table, OperationBase> =
        adapter.where<Table, OperationBase>(this).whereExists(not, whereable)

    override fun constrain(offset: Long, limit: Long): WhereWithOffset<Table, OperationBase> =
        adapter.where<Table, OperationBase>(this).constrain(offset, limit)
}