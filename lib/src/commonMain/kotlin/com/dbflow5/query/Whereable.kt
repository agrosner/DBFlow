package com.dbflow5.query

import com.dbflow5.adapter.DBRepresentable
import com.dbflow5.database.DatabaseConnection
import com.dbflow5.query.operations.AnyOperator
import com.dbflow5.query.operations.Property

/**
 * Description:
 */
interface Whereable<Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>,
    Representable : DBRepresentable<Table>> :
    HasAdapter<Table, Representable>,
    GroupByEnabled<Table, Result, OperationBase, Representable>,
    HavingEnabled<Table, Result, OperationBase, Representable>,
    Limitable<Table, Result, OperationBase, Representable>,
    Offsettable<Table, Result, OperationBase, Representable>,
    OrderByEnabled<Table, Result, OperationBase, Representable>,
    WhereExistsEnabled<Table, Result, OperationBase, Representable>,
    Constrainable<Table, Result, OperationBase, Representable>,
    ExecutableQuery<Result>,
    WhereBase<Result> {

    infix fun where(operator: AnyOperator): WhereStart<Table, Result, OperationBase, Representable> =
        adapter.where(this, resultFactory, operator)

    fun where(vararg operators: AnyOperator): WhereStart<Table, Result, OperationBase, Representable> =
        adapter.where(this, resultFactory, *operators)

    override fun groupBy(nameAlias: NameAlias): WhereWithGroupBy<Table, Result, OperationBase, Representable> =
        adapter.where<Table, Result, OperationBase, Representable>(this, resultFactory).groupBy(nameAlias)

    override fun groupBy(vararg nameAliases: NameAlias): WhereWithGroupBy<Table, Result, OperationBase, Representable> =
        adapter.where<Table, Result, OperationBase, Representable>(this, resultFactory).groupBy(*nameAliases)

    override fun groupBy(property: Property<*, Table>): WhereWithGroupBy<Table, Result, OperationBase, Representable> =
        adapter.where<Table, Result, OperationBase, Representable>(this, resultFactory).groupBy(property)

    override fun groupBy(vararg properties: Property<*, Table>): WhereWithGroupBy<Table, Result, OperationBase, Representable> =
        adapter.where<Table, Result, OperationBase, Representable>(this, resultFactory).groupBy(*properties)

    override fun having(operator: AnyOperator): WhereWithHaving<Table, Result, OperationBase, Representable> =
        adapter.where<Table, Result, OperationBase, Representable>(this, resultFactory).having(operator)

    override fun having(vararg operators: AnyOperator): WhereWithHaving<Table, Result, OperationBase, Representable> =
        adapter.where<Table, Result, OperationBase, Representable>(this, resultFactory).having(*operators)

    override fun limit(count: Long): WhereWithLimit<Table, Result, OperationBase, Representable> =
        adapter.where<Table, Result, OperationBase, Representable>(this, resultFactory).limit(count)

    override fun offset(offset: Long): WhereWithOffset<Table, Result, OperationBase, Representable> =
        adapter.where<Table, Result, OperationBase, Representable>(this, resultFactory).offset(offset)

    override fun orderBy(orderBy: OrderBy): WhereWithOrderBy<Table, Result, OperationBase, Representable> =
        adapter.where<Table, Result, OperationBase, Representable>(this, resultFactory).orderBy(orderBy)

    override fun orderBy(vararg orderBies: OrderBy): WhereWithOrderBy<Table, Result, OperationBase, Representable> =
        adapter.where<Table, Result, OperationBase, Representable>(this, resultFactory).orderBy(*orderBies)

    override fun orderBy(
        nameAlias: NameAlias,
        ascending: Boolean
    ): WhereWithOrderBy<Table, Result, OperationBase, Representable> =
        adapter.where<Table, Result, OperationBase, Representable>(this, resultFactory)
            .orderBy(nameAlias, ascending)

    override fun orderBy(
        property: Property<*, Table>,
        ascending: Boolean
    ): WhereWithOrderBy<Table, Result, OperationBase, Representable> =
        adapter.where<Table, Result, OperationBase, Representable>(this, resultFactory)
            .orderBy(property, ascending)

    override fun orderByAll(orderByList: List<OrderBy>): WhereWithOrderBy<Table, Result, OperationBase, Representable> =
        adapter.where<Table, Result, OperationBase, Representable>(this, resultFactory).orderByAll(orderByList)

    override fun <OtherTable : Any, OtherOperationBase : ExecutableQuery<Result>> whereExists(
        whereable: Where<OtherTable, Result, OtherOperationBase, *>
    ): WhereExists<Table, Result, OperationBase, Representable> =
        adapter.where<Table, Result, OperationBase, Representable>(this, resultFactory).whereExists(whereable)

    override fun <OtherTable : Any, OtherOperationBase : ExecutableQuery<Result>> whereExists(
        not: Boolean,
        whereable: Where<OtherTable, Result, OtherOperationBase, *>
    ): WhereExists<Table, Result, OperationBase, Representable> =
        adapter.where<Table, Result, OperationBase, Representable>(this, resultFactory).whereExists(not, whereable)

    override fun constrain(
        offset: Long,
        limit: Long
    ): WhereWithOffset<Table, Result, OperationBase, Representable> =
        adapter.where<Table, Result, OperationBase, Representable>(this, resultFactory).constrain(offset, limit)

    override suspend fun execute(db: DatabaseConnection): Result =
        adapter.where<Table, Result, OperationBase, Representable>(this, resultFactory).execute(db)
}