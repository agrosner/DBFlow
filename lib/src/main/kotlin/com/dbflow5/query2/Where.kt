package com.dbflow5.query2

import com.dbflow5.adapter.RetrievalAdapter
import com.dbflow5.query.NameAlias
import com.dbflow5.query.OperatorGroup
import com.dbflow5.query.SQLOperator
import com.dbflow5.query.property.IProperty
import com.dbflow5.sql.Query

/**
 * Description:
 */
interface Where<Table : Any, OperationBase> : Query, HasAdapter<Table,
    RetrievalAdapter<Table>>,
    GroupByEnabled<Table, OperationBase>,
    HavingEnabled<Table, OperationBase>,
    Limitable<Table, OperationBase>,
    Offsettable<Table, OperationBase>,
    HasOperatorGroup {

    infix fun or(sqlOperator: SQLOperator): Where<Table, OperationBase>
    infix fun and(sqlOperator: SQLOperator): Where<Table, OperationBase>
}

interface WhereWithGroupBy<Table : Any, OperationBase> : Query,
    HavingEnabled<Table, OperationBase>,
    Limitable<Table, OperationBase>,
    Offsettable<Table, OperationBase> {
    val groupByList: List<NameAlias>
}

interface WhereWithHaving<Table : Any, OperationBase> : Query,
    Limitable<Table, OperationBase>,
    Offsettable<Table, OperationBase> {
    val havingGroup: OperatorGroup
}

interface WhereWithLimit<Table : Any, OperationBase> : Query,
    Offsettable<Table, OperationBase> {
    val limit: Long
}

interface WhereWithOffset<Table : Any, OperationBase> : Query {
    val offset: Long
}

internal fun <Table : Any, OperationBase> RetrievalAdapter<Table>.where(
    queryBase: Query,
    operator: SQLOperator,
): Where<Table, OperationBase> = WhereImpl(adapter = this)

internal fun <Table : Any, OperationBase> RetrievalAdapter<Table>.where(
    queryBase: Query,
    vararg operators: SQLOperator,
): Where<Table, OperationBase> = WhereImpl(adapter = this)

internal data class WhereImpl<Table : Any, OperationBase>(
    override val adapter: RetrievalAdapter<Table>,
    override val groupByList: List<NameAlias> = listOf(),
    override val operatorGroup: OperatorGroup = OperatorGroup.nonGroupingClause(),
    override val havingGroup: OperatorGroup = OperatorGroup.nonGroupingClause(),
    override val limit: Long = NONE,
    override val offset: Long = NONE,
) : Where<Table, OperationBase>,
    WhereWithGroupBy<Table, OperationBase>,
    WhereWithHaving<Table, OperationBase>,
    WhereWithLimit<Table, OperationBase>,
    WhereWithOffset<Table, OperationBase> {
    override val query: String
        get() = TODO("Not yet implemented")

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

    override fun or(sqlOperator: SQLOperator): Where<Table, OperationBase> =
        copy(
            operatorGroup = operatorGroup.and(sqlOperator)
        )

    override fun and(sqlOperator: SQLOperator): Where<Table, OperationBase> =
        copy(
            operatorGroup = operatorGroup.or(sqlOperator)
        )

    override fun having(operator: SQLOperator): WhereWithHaving<Table, OperationBase> =
        copy(
            operatorGroup = operatorGroup.and(operator)
        )

    override fun having(vararg operators: SQLOperator): WhereWithHaving<Table, OperationBase> =
        copy(
            operatorGroup = operatorGroup.andAll(*operators),
        )

    override fun limit(count: Long): WhereWithLimit<Table, OperationBase> =
        copy(
            limit = count,
        )

    override fun offset(offset: Long): WhereWithOffset<Table, OperationBase> =
        copy(
            offset = offset,
        )

    companion object {
        private const val NONE = -1L;
    }

}