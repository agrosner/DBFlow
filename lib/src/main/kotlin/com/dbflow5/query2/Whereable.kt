package com.dbflow5.query2

import com.dbflow5.adapter.SQLObjectAdapter
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.query.NameAlias
import com.dbflow5.query.OrderBy
import com.dbflow5.query2.operations.AnyOperator
import com.dbflow5.query2.operations.Property

/**
 * Description:
 */
interface Whereable<Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>> :
    HasAdapter<Table, SQLObjectAdapter<Table>>,
    GroupByEnabled<Table, Result, OperationBase>,
    HavingEnabled<Table, Result, OperationBase>,
    Limitable<Table, Result, OperationBase>,
    Offsettable<Table, Result, OperationBase>,
    OrderByEnabled<Table, Result, OperationBase>,
    WhereExistsEnabled<Table, Result, OperationBase>,
    Constrainable<Table, Result, OperationBase>,
    ExecutableQuery<Result>,
    WhereBase<Result> {

    infix fun where(operator: AnyOperator): WhereStart<Table, Result, OperationBase> =
        adapter.where(this, resultFactory, operator)

    fun where(vararg operators: AnyOperator): WhereStart<Table, Result, OperationBase> =
        adapter.where(this, resultFactory, *operators)

    override fun groupBy(nameAlias: NameAlias): WhereWithGroupBy<Table, Result, OperationBase> =
        adapter.where<Table, Result, OperationBase>(this, resultFactory).groupBy(nameAlias)

    override fun groupBy(vararg nameAliases: NameAlias): WhereWithGroupBy<Table, Result, OperationBase> =
        adapter.where<Table, Result, OperationBase>(this, resultFactory).groupBy(*nameAliases)

    override fun groupBy(property: Property<*, Table>): WhereWithGroupBy<Table, Result, OperationBase> =
        adapter.where<Table, Result, OperationBase>(this, resultFactory).groupBy(property)

    override fun groupBy(vararg properties: Property<*, Table>): WhereWithGroupBy<Table, Result, OperationBase> =
        adapter.where<Table, Result, OperationBase>(this, resultFactory).groupBy(*properties)

    override fun having(operator: AnyOperator): WhereWithHaving<Table, Result, OperationBase> =
        adapter.where<Table, Result, OperationBase>(this, resultFactory).having(operator)

    override fun having(vararg operators: AnyOperator): WhereWithHaving<Table, Result, OperationBase> =
        adapter.where<Table, Result, OperationBase>(this, resultFactory).having(*operators)

    override fun limit(count: Long): WhereWithLimit<Table, Result, OperationBase> =
        adapter.where<Table, Result, OperationBase>(this, resultFactory).limit(count)

    override fun offset(offset: Long): WhereWithOffset<Table, Result, OperationBase> =
        adapter.where<Table, Result, OperationBase>(this, resultFactory).offset(offset)

    override fun orderBy(orderBy: OrderBy): WhereWithOrderBy<Table, Result, OperationBase> =
        adapter.where<Table, Result, OperationBase>(this, resultFactory).orderBy(orderBy)

    override fun orderBy(vararg orderBies: OrderBy): WhereWithOrderBy<Table, Result, OperationBase> =
        adapter.where<Table, Result, OperationBase>(this, resultFactory).orderBy(*orderBies)

    override fun orderBy(
        nameAlias: NameAlias,
        ascending: Boolean
    ): WhereWithOrderBy<Table, Result, OperationBase> =
        adapter.where<Table, Result, OperationBase>(this, resultFactory)
            .orderBy(nameAlias, ascending)

    override fun orderBy(
        property: Property<*, Table>,
        ascending: Boolean
    ): WhereWithOrderBy<Table, Result, OperationBase> =
        adapter.where<Table, Result, OperationBase>(this, resultFactory)
            .orderBy(property, ascending)

    override fun orderByAll(orderByList: List<OrderBy>): WhereWithOrderBy<Table, Result, OperationBase> =
        adapter.where<Table, Result, OperationBase>(this, resultFactory).orderByAll(orderByList)

    override fun <OtherTable : Any, OtherOperationBase : ExecutableQuery<Result>> whereExists(
        whereable: Where<OtherTable, Result, OtherOperationBase>
    ): WhereExists<Table, Result, OperationBase> =
        adapter.where<Table, Result, OperationBase>(this, resultFactory).whereExists(whereable)

    override fun <OtherTable : Any, OtherOperationBase : ExecutableQuery<Result>> whereExists(
        not: Boolean,
        whereable: Where<OtherTable, Result, OtherOperationBase>
    ): WhereExists<Table, Result, OperationBase> =
        adapter.where<Table, Result, OperationBase>(this, resultFactory).whereExists(not, whereable)

    override fun constrain(
        offset: Long,
        limit: Long
    ): WhereWithOffset<Table, Result, OperationBase> =
        adapter.where<Table, Result, OperationBase>(this, resultFactory).constrain(offset, limit)

    override suspend fun execute(db: DatabaseWrapper): Result =
        adapter.where<Table, Result, OperationBase>(this, resultFactory).execute(db)
}