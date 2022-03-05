package com.dbflow5.query

import com.dbflow5.adapter.DBRepresentable
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.query.operations.AnyOperator
import com.dbflow5.query.operations.Operation
import com.dbflow5.query.operations.OperatorGroup
import com.dbflow5.query.operations.OperatorGrouping
import com.dbflow5.query.operations.Property
import com.dbflow5.sql.Query

/**
 * Required implementation details for base where use.
 */
interface WhereBase<Result> : Query, HasAssociatedAdapters<DBRepresentable<*>> {
    val resultFactory: ResultFactory<Result>
}

/**
 * Where all terminal Where queries end up in
 */
interface Where<Table : Any, Result, OperationBase : ExecutableQuery<Result>,
    Representable : DBRepresentable<Table>> :
    ExecutableQuery<Result>,
    HasAdapter<Table, Representable>,
    HasAssociatedAdapters<DBRepresentable<*>>,
    Constrainable<Table, Result, OperationBase, Representable>

interface WhereStart<Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>,
    Representable : DBRepresentable<Table>> :
    Where<Table, Result, OperationBase, Representable>,
    GroupByEnabled<Table, Result, OperationBase, Representable>,
    HavingEnabled<Table, Result, OperationBase, Representable>,
    Limitable<Table, Result, OperationBase, Representable>,
    Offsettable<Table, Result, OperationBase, Representable>,
    OrderByEnabled<Table, Result, OperationBase, Representable>,
    WhereExistsEnabled<Table, Result, OperationBase, Representable>,
    HasOperatorGroup {

    infix fun or(sqlOperator: AnyOperator): WhereStart<Table, Result, OperationBase, Representable>
    infix fun and(sqlOperator: AnyOperator): WhereStart<Table, Result, OperationBase, Representable>
}

interface WhereExists<Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>,
    Representable : DBRepresentable<Table>> :
    Where<Table, Result, OperationBase, Representable>,
    GroupByEnabled<Table, Result, OperationBase, Representable>,
    HavingEnabled<Table, Result, OperationBase, Representable>,
    Limitable<Table, Result, OperationBase, Representable>,
    Offsettable<Table, Result, OperationBase, Representable>,
    OrderByEnabled<Table, Result, OperationBase, Representable>,
    Constrainable<Table, Result, OperationBase, Representable> {
    val existsWhere: Where<*, *, *, *>?

    /**
     * If true, use NOT EXISTS
     */
    val isNotWhere: Boolean
}

interface WhereWithGroupBy<Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>,
    Representable : DBRepresentable<Table>> :
    Where<Table, Result, OperationBase, Representable>,
    HavingEnabled<Table, Result, OperationBase, Representable>,
    Limitable<Table, Result, OperationBase, Representable>,
    Offsettable<Table, Result, OperationBase, Representable>,
    OrderByEnabled<Table, Result, OperationBase, Representable>,
    Constrainable<Table, Result, OperationBase, Representable> {
    val groupByList: List<NameAlias>
}

interface WhereWithOrderBy<Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>,
    Representable : DBRepresentable<Table>> :
    Where<Table, Result, OperationBase, Representable>,
    HavingEnabled<Table, Result, OperationBase, Representable>,
    Limitable<Table, Result, OperationBase, Representable>,
    Offsettable<Table, Result, OperationBase, Representable>,
    Constrainable<Table, Result, OperationBase, Representable> {

    val orderByList: List<OrderBy>
}

interface WhereWithHaving<Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>,
    Representable : DBRepresentable<Table>> :
    Where<Table, Result, OperationBase, Representable>,
    Limitable<Table, Result, OperationBase, Representable>,
    Offsettable<Table, Result, OperationBase, Representable>,
    Constrainable<Table, Result, OperationBase, Representable> {
    val havingGroup: OperatorGrouping<Query>
}

interface WhereWithLimit<Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>,
    Representable : DBRepresentable<Table>> :
    Where<Table, Result, OperationBase, Representable>,
    Offsettable<Table, Result, OperationBase, Representable> {
    val limit: Long
}

interface WhereWithOffset<Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>,
    Representable : DBRepresentable<Table>> :
    Where<Table, Result, OperationBase, Representable> {
    val offset: Long
}

internal fun <Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>,
    Representable : DBRepresentable<Table>> Representable.where(
    queryBase: WhereBase<Result>,
    resultFactory: ResultFactory<Result>,
    vararg operators: AnyOperator,
): WhereStart<Table, Result, OperationBase, Representable> = WhereImpl(
    adapter = this,
    queryBase = queryBase,
    resultFactory = resultFactory,
    operatorGroup = OperatorGroup.nonGroupingClause()
        .chain(Operation.And, *operators)
)

internal data class WhereImpl<Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>,
    Representable : DBRepresentable<Table>>(
    private val queryBase: WhereBase<Result>,
    private val resultFactory: ResultFactory<Result>,
    override val adapter: Representable,
    override val groupByList: List<NameAlias> = listOf(),
    override val operatorGroup: OperatorGrouping<Query> = OperatorGroup.nonGroupingClause(),
    override val havingGroup: OperatorGrouping<Query> = OperatorGroup.nonGroupingClause(),
    override val limit: Long = NONE,
    override val offset: Long = NONE,
    override val orderByList: List<OrderBy> = listOf(),
    override val existsWhere: Where<*, *, *, *>? = null,
    override val isNotWhere: Boolean = false,
) : WhereStart<Table, Result, OperationBase, Representable>,
    WhereWithGroupBy<Table, Result, OperationBase, Representable>,
    WhereWithHaving<Table, Result, OperationBase, Representable>,
    WhereWithLimit<Table, Result, OperationBase, Representable>,
    WhereWithOffset<Table, Result, OperationBase, Representable>,
    WhereWithOrderBy<Table, Result, OperationBase, Representable>,
    WhereExists<Table, Result, OperationBase, Representable>,
    HasAssociatedAdapters<DBRepresentable<*>> by queryBase {
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
            orderByList.joinToString(separator = ",") { it.query }
                .takeIf { it.isNotBlank() }
                ?.let { orderByList -> append("ORDER BY $orderByList ") }

            if (limit > NONE) append("LIMIT $limit ")
            if (offset > NONE) append("OFFSET $offset ")
        }
    }

    override fun groupBy(nameAlias: NameAlias): WhereWithGroupBy<Table, Result, OperationBase, Representable> =
        copy(
            groupByList = groupByList.toMutableList().apply { add(nameAlias) }
        )

    override fun groupBy(
        vararg nameAliases: NameAlias,
    ): WhereWithGroupBy<Table, Result, OperationBase, Representable> =
        copy(
            groupByList = groupByList.toMutableList().apply { addAll(nameAliases) }
        )

    override fun groupBy(
        property: Property<*, Table>,
    ): WhereWithGroupBy<Table, Result, OperationBase, Representable> =
        copy(
            groupByList = groupByList.toMutableList().apply { add(property.nameAlias) }
        )

    override fun groupBy(
        vararg properties: Property<*, Table>,
    ): WhereWithGroupBy<Table, Result, OperationBase, Representable> =
        copy(
            groupByList = groupByList.toMutableList()
                .apply { addAll(properties.map { it.nameAlias }) }
        )

    override fun or(
        sqlOperator: AnyOperator,
    ): WhereStart<Table, Result, OperationBase, Representable> =
        copy(
            operatorGroup = operatorGroup.or(sqlOperator)
        )

    override fun and(
        sqlOperator: AnyOperator,
    ): WhereStart<Table, Result, OperationBase, Representable> =
        copy(
            operatorGroup = operatorGroup.and(sqlOperator)
        )

    override fun having(
        operator: AnyOperator,
    ): WhereWithHaving<Table, Result, OperationBase, Representable> =
        copy(
            havingGroup = havingGroup.and(operator)
        )

    override fun having(
        vararg operators: AnyOperator,
    ): WhereWithHaving<Table, Result, OperationBase, Representable> =
        copy(
            havingGroup = havingGroup.chain(Operation.And, *operators),
        )

    override fun limit(count: Long): WhereWithLimit<Table, Result, OperationBase, Representable> =
        copy(
            limit = count,
        )

    override fun offset(offset: Long): WhereWithOffset<Table, Result, OperationBase, Representable> =
        copy(
            offset = offset,
        )

    override fun orderBy(
        orderBy: OrderBy,
    ): WhereWithOrderBy<Table, Result, OperationBase, Representable> =
        copy(
            orderByList = listOf(orderBy),
        )

    override fun orderBy(
        vararg orderBies: OrderBy,
    ): WhereWithOrderBy<Table, Result, OperationBase, Representable> =
        copy(
            orderByList = orderBies.toList(),
        )

    override fun orderBy(
        nameAlias: NameAlias,
        ascending: Boolean
    ): WhereWithOrderBy<Table, Result, OperationBase, Representable> =
        copy(
            orderByList = listOf(com.dbflow5.query.orderBy(nameAlias).direction(ascending)),
        )

    override fun orderBy(
        property: Property<*, Table>,
        ascending: Boolean
    ): WhereWithOrderBy<Table, Result, OperationBase, Representable> =
        copy(
            orderByList = listOf(
                com.dbflow5.query.orderBy(property).direction(ascending)
            ),
        )

    override fun orderByAll(
        orderByList: List<OrderBy>,
    ): WhereWithOrderBy<Table, Result, OperationBase, Representable> =
        copy(
            orderByList = orderByList,
        )

    override fun <OtherTable : Any, OperationBase : ExecutableQuery<Result>>
        whereExists(whereable: Where<OtherTable, Result, OperationBase, *>) =
        copy(
            existsWhere = whereable,
        )

    override fun <OtherTable : Any, OtherOperationBase : ExecutableQuery<Result>> whereExists(
        not: Boolean,
        whereable: Where<OtherTable, Result, OtherOperationBase, *>
    ): WhereExists<Table, Result, OperationBase, Representable> =
        copy(
            existsWhere = whereable,
            isNotWhere = not,
        )

    override fun constrain(
        offset: Long,
        limit: Long
    ): WhereWithOffset<Table, Result, OperationBase, Representable> =
        limit(limit).offset(offset)

    override suspend fun execute(db: DatabaseWrapper): Result =
        resultFactory.run { db.createResult(query) }

    companion object {
        private const val NONE = -1L;
    }

}