package com.dbflow5.query2

import com.dbflow5.adapter.SQLObjectAdapter
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.query.NameAlias
import com.dbflow5.query.OperatorGroup
import com.dbflow5.query.OrderBy
import com.dbflow5.query.SQLOperator
import com.dbflow5.query.property.IProperty
import com.dbflow5.sql.Query

/**
 * Required implementation details for base where use.
 */
interface WhereBase<Result> : Query, HasAssociatedAdapters {
    val resultFactory: ResultFactory<Result>
}

/**
 * Where all terminal Where queries end up in
 */
interface Where<Table : Any, Result, OperationBase : ExecutableQuery<Result>> :
    ExecutableQuery<Result>,
    HasAdapter<Table, SQLObjectAdapter<Table>>,
    HasAssociatedAdapters,
    Constrainable<Table, Result, OperationBase>

interface WhereStart<Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>> :
    Where<Table, Result, OperationBase>,
    GroupByEnabled<Table, Result, OperationBase>,
    HavingEnabled<Table, Result, OperationBase>,
    Limitable<Table, Result, OperationBase>,
    Offsettable<Table, Result, OperationBase>,
    OrderByEnabled<Table, Result, OperationBase>,
    WhereExistsEnabled<Table, Result, OperationBase>,
    HasOperatorGroup {

    infix fun or(sqlOperator: SQLOperator): WhereStart<Table, Result, OperationBase>
    infix fun and(sqlOperator: SQLOperator): WhereStart<Table, Result, OperationBase>
}

interface WhereExists<Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>> :
    Where<Table, Result, OperationBase>,
    GroupByEnabled<Table, Result, OperationBase>,
    HavingEnabled<Table, Result, OperationBase>,
    Limitable<Table, Result, OperationBase>,
    Offsettable<Table, Result, OperationBase>,
    OrderByEnabled<Table, Result, OperationBase>,
    Constrainable<Table, Result, OperationBase> {
    val existsWhere: Where<*, *, *>?

    /**
     * If true, use NOT EXISTS
     */
    val isNotWhere: Boolean
}

interface WhereWithGroupBy<Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>> :
    Where<Table, Result, OperationBase>,
    HavingEnabled<Table, Result, OperationBase>,
    Limitable<Table, Result, OperationBase>,
    Offsettable<Table, Result, OperationBase>,
    OrderByEnabled<Table, Result, OperationBase>,
    Constrainable<Table, Result, OperationBase> {
    val groupByList: List<NameAlias>
}

interface WhereWithOrderBy<Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>> :
    Where<Table, Result, OperationBase>,
    HavingEnabled<Table, Result, OperationBase>,
    Limitable<Table, Result, OperationBase>,
    Offsettable<Table, Result, OperationBase>,
    Constrainable<Table, Result, OperationBase> {

    val orderByList: List<OrderBy>
}

interface WhereWithHaving<Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>> :
    Where<Table, Result, OperationBase>,
    Limitable<Table, Result, OperationBase>,
    Offsettable<Table, Result, OperationBase>,
    Constrainable<Table, Result, OperationBase> {
    val havingGroup: OperatorGroup
}

interface WhereWithLimit<Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>> :
    Where<Table, Result, OperationBase>,
    Offsettable<Table, Result, OperationBase> {
    val limit: Long
}

interface WhereWithOffset<Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>> :
    Where<Table, Result, OperationBase> {
    val offset: Long
}

internal fun <Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>> SQLObjectAdapter<Table>.where(
    queryBase: WhereBase<Result>,
    resultFactory: ResultFactory<Result>,
    vararg operators: SQLOperator,
): WhereStart<Table, Result, OperationBase> = WhereImpl(
    adapter = this,
    queryBase = queryBase,
    resultFactory = resultFactory,
    operatorGroup = OperatorGroup.nonGroupingClause().andAll(*operators)
)

internal data class WhereImpl<Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>>(
    private val queryBase: WhereBase<Result>,
    private val resultFactory: ResultFactory<Result>,
    override val adapter: SQLObjectAdapter<Table>,
    override val groupByList: List<NameAlias> = listOf(),
    override val operatorGroup: OperatorGroup = OperatorGroup.nonGroupingClause(),
    override val havingGroup: OperatorGroup = OperatorGroup.nonGroupingClause(),
    override val limit: Long = NONE,
    override val offset: Long = NONE,
    override val orderByList: List<OrderBy> = listOf(),
    override val existsWhere: Where<*, *, *>? = null,
    override val isNotWhere: Boolean = false,
) : WhereStart<Table, Result, OperationBase>,
    WhereWithGroupBy<Table, Result, OperationBase>,
    WhereWithHaving<Table, Result, OperationBase>,
    WhereWithLimit<Table, Result, OperationBase>,
    WhereWithOffset<Table, Result, OperationBase>,
    WhereWithOrderBy<Table, Result, OperationBase>,
    WhereExists<Table, Result, OperationBase>,
    HasAssociatedAdapters by queryBase {
    override val query: String by lazy {
        buildString {
            append("${queryBase.query.trim()} ")
            if (existsWhere != null) {
                append("WHERE ${if (isNotWhere) "NOT " else ""}EXISTS (${existsWhere.query.trim()}) ")
            } else if (operatorGroup.query.isNotBlank()) {
                append("WHERE ${operatorGroup.query} ")
            }
            groupByList.joinToString(separator = ",")
                .takeIf { it.isNotBlank() }
                ?.let { groupByList -> append("GROUP BY $groupByList ") }

            if (havingGroup.query.isNotBlank()) {
                append("HAVING ${havingGroup.query} ")
            }
            orderByList.joinToString(separator = ",")
                .takeIf { it.isNotBlank() }
                ?.let { orderByList -> append("ORDER BY $orderByList ") }

            if (limit > NONE) append("LIMIT $limit ")
            if (offset > NONE) append("OFFSET $offset ")
        }
    }

    override fun groupBy(nameAlias: NameAlias): WhereWithGroupBy<Table, Result, OperationBase> =
        copy(
            groupByList = groupByList.toMutableList().apply { add(nameAlias) }
        )

    override fun groupBy(vararg nameAliases: NameAlias): WhereWithGroupBy<Table, Result, OperationBase> =
        copy(
            groupByList = groupByList.toMutableList().apply { addAll(nameAliases) }
        )

    override fun groupBy(property: IProperty<*>): WhereWithGroupBy<Table, Result, OperationBase> =
        copy(
            groupByList = groupByList.toMutableList().apply { add(property.nameAlias) }
        )

    override fun groupBy(vararg properties: IProperty<*>): WhereWithGroupBy<Table, Result, OperationBase> =
        copy(
            groupByList = groupByList.toMutableList()
                .apply { addAll(properties.map { it.nameAlias }) }
        )

    override fun or(sqlOperator: SQLOperator): WhereStart<Table, Result, OperationBase> =
        copy(
            operatorGroup = operatorGroup.or(sqlOperator)
        )

    override fun and(sqlOperator: SQLOperator): WhereStart<Table, Result, OperationBase> =
        copy(
            operatorGroup = operatorGroup.and(sqlOperator)
        )

    override fun having(operator: SQLOperator): WhereWithHaving<Table, Result, OperationBase> =
        copy(
            havingGroup = havingGroup.and(operator)
        )

    override fun having(vararg operators: SQLOperator): WhereWithHaving<Table, Result, OperationBase> =
        copy(
            havingGroup = havingGroup.andAll(*operators),
        )

    override fun limit(count: Long): WhereWithLimit<Table, Result, OperationBase> =
        copy(
            limit = count,
        )

    override fun offset(offset: Long): WhereWithOffset<Table, Result, OperationBase> =
        copy(
            offset = offset,
        )

    override fun orderBy(orderBy: OrderBy): WhereWithOrderBy<Table, Result, OperationBase> =
        copy(
            orderByList = listOf(orderBy),
        )

    override fun orderBy(vararg orderBies: OrderBy): WhereWithOrderBy<Table, Result, OperationBase> =
        copy(
            orderByList = orderBies.toList(),
        )

    override fun orderBy(
        nameAlias: NameAlias,
        ascending: Boolean
    ): WhereWithOrderBy<Table, Result, OperationBase> =
        copy(
            orderByList = listOf(OrderBy.fromNameAlias(nameAlias, isAscending = ascending)),
        )

    override fun orderBy(
        property: IProperty<*>,
        ascending: Boolean
    ): WhereWithOrderBy<Table, Result, OperationBase> =
        copy(
            orderByList = listOf(OrderBy.fromProperty(property, isAscending = ascending)),
        )


    override fun orderByAll(orderByList: List<OrderBy>): WhereWithOrderBy<Table, Result, OperationBase> =
        copy(
            orderByList = orderByList,
        )

    override fun <OtherTable : Any, OperationBase : ExecutableQuery<Result>> whereExists(whereable: Where<OtherTable, Result, OperationBase>) =
        copy(
            existsWhere = whereable,
        )

    override fun <OtherTable : Any, OtherOperationBase : ExecutableQuery<Result>> whereExists(
        not: Boolean,
        whereable: Where<OtherTable, Result, OtherOperationBase>
    ): WhereExists<Table, Result, OperationBase> =
        copy(
            existsWhere = whereable,
            isNotWhere = not,
        )

    override fun constrain(
        offset: Long,
        limit: Long
    ): WhereWithOffset<Table, Result, OperationBase> =
        limit(limit).offset(offset)

    override suspend fun execute(db: DatabaseWrapper): Result =
        resultFactory.run { db.createResult(query) }

    companion object {
        private const val NONE = -1L;
    }

}