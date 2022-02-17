package com.dbflow5.query

import com.dbflow5.adapter.SQLObjectAdapter
import com.dbflow5.query.operations.AnyOperator
import com.dbflow5.query.operations.Operation
import com.dbflow5.query.operations.OperatorGroup
import com.dbflow5.query.operations.OperatorGrouping
import com.dbflow5.query.operations.Property
import com.dbflow5.sql.Query


interface JoinOn<OriginalTable : Any, Result> {
    infix fun on(sqlOperator: AnyOperator): SelectWithJoins<OriginalTable, Result>
    fun on(vararg conditions: AnyOperator): SelectWithJoins<OriginalTable, Result>
}

interface JoinUsing<OriginalTable : Any, Result> {
    infix fun using(property: Property<*, OriginalTable>): SelectWithJoins<OriginalTable, Result>
    fun using(vararg properties: Property<*, OriginalTable>): SelectWithJoins<OriginalTable, Result>
}

interface JoinWithAlias<OriginalTable : Any,
    JoinTable : Any, Result> : Query,
    JoinUsing<OriginalTable, Result>,
    JoinOn<OriginalTable, Result> {
    val alias: NameAlias
}

interface JoinWithUsing : Query {
    val using: List<Property<*, *>>
}

/**
 * Description:
 */
interface Join<OriginalTable : Any, JoinTable : Any, Result> : Query,
    JoinOn<OriginalTable, Result>, HasAdapter<JoinTable, SQLObjectAdapter<JoinTable>>,
    Aliasable<JoinWithAlias<OriginalTable, JoinTable, Result>>,
    JoinUsing<OriginalTable, Result> {
    fun end(): SelectWithJoins<OriginalTable, Result>
}

internal data class JoinImpl<OriginalTable : Any, JoinTable : Any, Result>(
    val select: SelectImpl<OriginalTable, Result>,
    val type: JoinType,
    val onGroup: OperatorGrouping<Query>? = null,
    override val adapter: SQLObjectAdapter<JoinTable>,
    private val queryNameAlias: NameAlias? = null,
    override val alias: NameAlias = queryNameAlias ?: NameAlias.Builder(adapter.name).build(),
    override val using: List<Property<*, OriginalTable>> = listOf(),
) : Join<OriginalTable, JoinTable, Result>, JoinOn<OriginalTable, Result>,
    JoinWithAlias<OriginalTable, JoinTable, Result>,
    JoinWithUsing {
    override val query: String by lazy {
        buildString {
            append("${type.value} JOIN ${alias.fullQuery} ")
            if (type != JoinType.Natural) {
                onGroup?.let { append("ON ${onGroup.query} ") }
                    ?: if (using.isNotEmpty()) {
                        append("USING (${using.joinToString { it.query }}) ")
                    }
            }
        }
    }

    private fun checkNatural() {
        // TODO: code smell!
        if (type == JoinType.Natural) {
            throw IllegalArgumentException(
                "Cannot specify a clause for this join if its NATURAL."
                    + " Specifying a clause would have no effect. Call end() to continue the query."
            )
        }
    }

    private fun addJoin(join: Join<OriginalTable, JoinTable, Result>): SelectWithJoins<OriginalTable, Result> =
        select.copy(
            joins = select.joins.toMutableList().apply {
                add(join)
            },
        )

    override fun on(sqlOperator: AnyOperator): SelectWithJoins<OriginalTable, Result> {
        checkNatural()
        return addJoin(
            copy(
                onGroup = OperatorGroup
                    .nonGroupingClause()
                    .and(sqlOperator),
            )
        )
    }

    override fun on(vararg conditions: AnyOperator): SelectWithJoins<OriginalTable, Result> {
        checkNatural()
        return addJoin(
            copy(
                onGroup = OperatorGroup
                    .nonGroupingClause()
                    .chain(Operation.And, *conditions),
            )
        )
    }


    override fun `as`(
        name: String,
        shouldAddIdentifierToAlias: Boolean
    ): JoinWithAlias<OriginalTable, JoinTable, Result> =
        copy(
            alias = alias.newBuilder()
                .shouldAddIdentifierToAliasName(shouldAddIdentifierToAlias)
                .`as`(name).build(),
        )

    override fun using(property: Property<*, OriginalTable>): SelectWithJoins<OriginalTable, Result> {
        checkNatural()
        return addJoin(
            copy(
                using = using.toMutableList()
                    .apply { add(property) }
            )
        )
    }

    override fun using(vararg properties: Property<*, OriginalTable>): SelectWithJoins<OriginalTable, Result> {
        checkNatural()
        return addJoin(
            copy(
                using = using.toMutableList()
                    .apply { addAll(properties) },
            )
        )
    }

    override fun end(): SelectWithJoins<OriginalTable, Result> = addJoin(this)
}