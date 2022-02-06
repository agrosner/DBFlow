package com.dbflow5.query2

import com.dbflow5.adapter.RetrievalAdapter
import com.dbflow5.query.NameAlias
import com.dbflow5.query.OperatorGroup
import com.dbflow5.query.OrderBy
import com.dbflow5.query.SQLOperator
import com.dbflow5.query.property.IProperty
import com.dbflow5.sql.Query

/**
 * Where all terminal Where queries end up in
 */
interface Where<Table : Any, OperationBase> : Query

interface WhereStart<Table : Any, OperationBase> :
    Where<Table, OperationBase>,
    HasAdapter<Table, RetrievalAdapter<Table>>,
    GroupByEnabled<Table, OperationBase>,
    HavingEnabled<Table, OperationBase>,
    Limitable<Table, OperationBase>,
    Offsettable<Table, OperationBase>,
    OrderByEnabled<Table, OperationBase>,
    WhereExistsEnabled<Table, OperationBase>,
    Constrainable<Table, OperationBase>,
    HasOperatorGroup {

    infix fun or(sqlOperator: SQLOperator): WhereStart<Table, OperationBase>
    infix fun and(sqlOperator: SQLOperator): WhereStart<Table, OperationBase>
}

interface WhereExists<Table : Any, OperationBase> :
    Where<Table, OperationBase>,
    GroupByEnabled<Table, OperationBase>,
    HavingEnabled<Table, OperationBase>,
    Limitable<Table, OperationBase>,
    Offsettable<Table, OperationBase>,
    OrderByEnabled<Table, OperationBase>,
    Constrainable<Table, OperationBase> {
    val existsWhere: Where<*, *>?

    /**
     * If true, use NOT EXISTS
     */
    val isNotWhere: Boolean
}

interface WhereWithGroupBy<Table : Any, OperationBase> :
    Where<Table, OperationBase>,
    HavingEnabled<Table, OperationBase>,
    Limitable<Table, OperationBase>,
    Offsettable<Table, OperationBase>,
    OrderByEnabled<Table, OperationBase>,
    Constrainable<Table, OperationBase> {
    val groupByList: List<NameAlias>
}

interface WhereWithOrderBy<Table : Any, OperationBase> :
    Where<Table, OperationBase>,
    HavingEnabled<Table, OperationBase>,
    Limitable<Table, OperationBase>,
    Offsettable<Table, OperationBase>,
    Constrainable<Table, OperationBase> {

    val orderByList: List<OrderBy>
}

interface WhereWithHaving<Table : Any, OperationBase> :
    Where<Table, OperationBase>,
    Limitable<Table, OperationBase>,
    Offsettable<Table, OperationBase>,
    Constrainable<Table, OperationBase> {
    val havingGroup: OperatorGroup
}

interface WhereWithLimit<Table : Any, OperationBase> :
    Where<Table, OperationBase>,
    Offsettable<Table, OperationBase> {
    val limit: Long
}

interface WhereWithOffset<Table : Any, OperationBase> :
    Where<Table, OperationBase> {
    val offset: Long
}

internal fun <Table : Any, OperationBase> RetrievalAdapter<Table>.where(
    queryBase: Query,
    operator: SQLOperator,
): WhereStart<Table, OperationBase> = WhereImpl(
    adapter = this,
    queryBase = queryBase,
    operatorGroup = OperatorGroup.nonGroupingClause().and(operator)
)

internal fun <Table : Any, OperationBase> RetrievalAdapter<Table>.where(
    queryBase: Query,
    vararg operators: SQLOperator,
): WhereStart<Table, OperationBase> = WhereImpl(
    adapter = this,
    queryBase = queryBase,
    operatorGroup = OperatorGroup.nonGroupingClause().andAll(*operators)
)

internal data class WhereImpl<Table : Any, OperationBase>(
    private val queryBase: Query,
    override val adapter: RetrievalAdapter<Table>,
    override val groupByList: List<NameAlias> = listOf(),
    override val operatorGroup: OperatorGroup = OperatorGroup.nonGroupingClause(),
    override val havingGroup: OperatorGroup = OperatorGroup.nonGroupingClause(),
    override val limit: Long = NONE,
    override val offset: Long = NONE,
    override val orderByList: List<OrderBy> = listOf(),
    override val existsWhere: Where<*, *>? = null,
    override val isNotWhere: Boolean = false,
) : WhereStart<Table, OperationBase>,
    WhereWithGroupBy<Table, OperationBase>,
    WhereWithHaving<Table, OperationBase>,
    WhereWithLimit<Table, OperationBase>,
    WhereWithOffset<Table, OperationBase>,
    WhereWithOrderBy<Table, OperationBase>,
    WhereExists<Table, OperationBase> {
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

    override fun groupBy(nameAlias: NameAlias): WhereWithGroupBy<Table, OperationBase> =
        copy(
            groupByList = groupByList.toMutableList().apply { add(nameAlias) }
        )

    override fun groupBy(vararg nameAliases: NameAlias): WhereWithGroupBy<Table, OperationBase> =
        copy(
            groupByList = groupByList.toMutableList().apply { addAll(nameAliases) }
        )

    override fun groupBy(property: IProperty<*>): WhereWithGroupBy<Table, OperationBase> =
        copy(
            groupByList = groupByList.toMutableList().apply { add(property.nameAlias) }
        )

    override fun groupBy(vararg properties: IProperty<*>): WhereWithGroupBy<Table, OperationBase> =
        copy(
            groupByList = groupByList.toMutableList()
                .apply { addAll(properties.map { it.nameAlias }) }
        )

    override fun or(sqlOperator: SQLOperator): WhereStart<Table, OperationBase> =
        copy(
            operatorGroup = operatorGroup.or(sqlOperator)
        )

    override fun and(sqlOperator: SQLOperator): WhereStart<Table, OperationBase> =
        copy(
            operatorGroup = operatorGroup.and(sqlOperator)
        )

    override fun having(operator: SQLOperator): WhereWithHaving<Table, OperationBase> =
        copy(
            havingGroup = havingGroup.and(operator)
        )

    override fun having(vararg operators: SQLOperator): WhereWithHaving<Table, OperationBase> =
        copy(
            havingGroup = havingGroup.andAll(*operators),
        )

    override fun limit(count: Long): WhereWithLimit<Table, OperationBase> =
        copy(
            limit = count,
        )

    override fun offset(offset: Long): WhereWithOffset<Table, OperationBase> =
        copy(
            offset = offset,
        )

    override fun orderBy(orderBy: OrderBy): WhereWithOrderBy<Table, OperationBase> =
        copy(
            orderByList = listOf(orderBy),
        )

    override fun orderBy(vararg orderBies: OrderBy): WhereWithOrderBy<Table, OperationBase> =
        copy(
            orderByList = orderBies.toList(),
        )

    override fun orderBy(
        nameAlias: NameAlias,
        ascending: Boolean
    ): WhereWithOrderBy<Table, OperationBase> =
        copy(
            orderByList = listOf(OrderBy.fromNameAlias(nameAlias, isAscending = ascending)),
        )

    override fun orderBy(
        property: IProperty<*>,
        ascending: Boolean
    ): WhereWithOrderBy<Table, OperationBase> =
        copy(
            orderByList = listOf(OrderBy.fromProperty(property, isAscending = ascending)),
        )


    override fun orderByAll(orderByList: List<OrderBy>): WhereWithOrderBy<Table, OperationBase> =
        copy(
            orderByList = orderByList,
        )

    override fun <OtherTable : Any, OperationBase> whereExists(whereable: Where<OtherTable, OperationBase>) =
        copy(
            existsWhere = whereable,
        )

    override fun <OtherTable : Any, OtherOperationBase> whereExists(
        not: Boolean,
        whereable: Where<OtherTable, OtherOperationBase>
    ): WhereExists<Table, OperationBase> =
        copy(
            existsWhere = whereable,
            isNotWhere = not,
        )

    override fun constrain(offset: Long, limit: Long): WhereWithOffset<Table, OperationBase> =
        limit(limit).offset(offset)

    companion object {
        private const val NONE = -1L;
    }

}