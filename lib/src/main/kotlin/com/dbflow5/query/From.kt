package com.dbflow5.query

import com.dbflow5.adapter.SQLObjectAdapter
import com.dbflow5.config.FlowManager
import com.dbflow5.query.Join.JoinType
import com.dbflow5.query.property.IndexProperty
import com.dbflow5.sql.Query
import com.dbflow5.structure.ChangeAction
import kotlin.reflect.KClass
import kotlin.collections.Set as KSet

/**
 * Description: The SQL FROM query wrapper that must have a [Query] base.
 */
class From<TModel : Any>
/**
 * The SQL from statement constructed.
 *
 * @param queryBuilderBase The base query we append this cursor to
 * @param table     The table this corresponds to
 */
internal constructor(

    /**
     * @return The base query, usually a [Delete], [Select], or [Update]
     */
    override val queryBuilderBase: Query,
    adapter: SQLObjectAdapter<TModel>,

    /**
     * If specified, we use this as the subquery for the FROM statement.
     */
    private val modelQueriable: ModelQueriable<TModel>? = null
) : BaseTransformable<TModel>(adapter) {

    /**
     * An alias for the table
     */
    private var tableAlias: NameAlias? = null

    /**
     * Enables the SQL JOIN statement
     */
    private val joins = mutableListOf<Join<*, *>>()

    override val primaryAction: ChangeAction
        get() = if (queryBuilderBase is Delete) ChangeAction.DELETE else ChangeAction.CHANGE

    override val query: String
        get() {
            val queryBuilder = StringBuilder()
                .append(queryBuilderBase.query)
            if (queryBuilderBase !is Update<*>) {
                queryBuilder.append("FROM ")
            }

            modelQueriable?.let { queryBuilder.append(it.enclosedQuery) }
                ?: queryBuilder.append(getTableAlias())

            if (queryBuilderBase is Select) {
                if (!joins.isEmpty()) {
                    queryBuilder.append(" ")
                }
                joins.forEach { queryBuilder.append(it.query) }
            } else {
                queryBuilder.append(" ")
            }

            return queryBuilder.toString()
        }

    override fun cloneSelf(): From<TModel> {
        val from = From(
            when (queryBuilderBase) {
                is Select -> queryBuilderBase.cloneSelf()
                else -> queryBuilderBase
            },
            adapter as SQLObjectAdapter<TModel>,
        )
        from.joins.addAll(joins)
        from.tableAlias = tableAlias
        return from
    }

    /**
     * @return A list of [Class] that represents tables represented in this query. For every
     * [Join] on another table, this adds another [Class].
     */
    val associatedTables: KSet<KClass<*>>
        get() = linkedSetOf<KClass<out Any>>(table).apply {
            joins.mapTo(this) { it.adapter.table }
        }

    private fun getTableAlias(): NameAlias = tableAlias
        ?: NameAlias.Builder(FlowManager.getTableName(table)).build().also { tableAlias = it }

    /**
     * Set an alias to the table name of this [From].
     */
    infix fun `as`(alias: String): From<TModel> {
        tableAlias = getTableAlias()
            .newBuilder()
            .`as`(alias)
            .build()
        return this
    }

    /**
     * Adds a join on a specific table for this query
     *
     * @param table    The table this corresponds to
     * @param joinType The type of join to use
     */
    fun <TJoin : Any> join(
        adapter: SQLObjectAdapter<TJoin>,
        joinType: JoinType
    ): Join<TJoin, TModel> {
        return Join(this, adapter, joinType).also { joins.add(it) }
    }

    /**
     * Adds a join on a specific table for this query.
     *
     * @param modelQueriable A query we construct the [Join] from.
     * @param joinType       The type of join to use.
     */
    fun <TJoin : Any> join(
        modelQueriable: ModelQueriable<TJoin>,
        joinType: JoinType
    ): Join<TJoin, TModel> {
        return Join(this, joinType, modelQueriable).also { joins.add(it) }
    }

    /**
     * Adds a [JoinType.CROSS] join on a specific table for this query.
     *
     * @param table   The table to join on.
     * @param <TJoin> The class of the join table.
    </TJoin> */
    infix fun <TJoin : Any> crossJoin(adapter: SQLObjectAdapter<TJoin>): Join<TJoin, TModel> =
        join(adapter, JoinType.CROSS)

    /**
     * Adds a [JoinType.CROSS] join on a specific table for this query.
     *
     * @param modelQueriable The query to join on.
     * @param <TJoin>        The class of the join table.
    </TJoin> */
    infix fun <TJoin : Any> crossJoin(modelQueriable: ModelQueriable<TJoin>): Join<TJoin, TModel> =
        join(modelQueriable, JoinType.CROSS)

    /**
     * Adds a [JoinType.INNER] join on a specific table for this query.
     *
     * @param table   The table to join on.
     * @param <TJoin> The class of the join table.
    </TJoin> */
    infix fun <TJoin : Any> innerJoin(adapter: SQLObjectAdapter<TJoin>): Join<TJoin, TModel> =
        join(adapter, JoinType.INNER)

    /**
     * Adds a [JoinType.INNER] join on a specific table for this query.
     *
     * @param modelQueriable The query to join on.
     * @param <TJoin>        The class of the join table.
    </TJoin> */
    infix fun <TJoin : Any> innerJoin(modelQueriable: ModelQueriable<TJoin>): Join<TJoin, TModel> =
        join(modelQueriable, JoinType.INNER)

    /**
     * Adds a [JoinType.LEFT_OUTER] join on a specific table for this query.
     *
     * @param table   The table to join on.
     * @param <TJoin> The class of the join table.
    </TJoin> */
    infix fun <TJoin : Any> leftOuterJoin(adapter: SQLObjectAdapter<TJoin>): Join<TJoin, TModel> =
        join(adapter, JoinType.LEFT_OUTER)

    /**
     * Adds a [JoinType.LEFT_OUTER] join on a specific table for this query.
     *
     * @param modelQueriable The query to join on.
     * @param <TJoin>        The class of the join table.
    </TJoin> */
    infix fun <TJoin : Any> leftOuterJoin(modelQueriable: ModelQueriable<TJoin>): Join<TJoin, TModel> =
        join(modelQueriable, JoinType.LEFT_OUTER)


    /**
     * Adds a [JoinType.NATURAL] join on a specific table for this query.
     *
     * @param adapter   The table to join on.
     * @param <TJoin> The class of the join table.
    </TJoin> */
    infix fun <TJoin : Any> naturalJoin(adapter: SQLObjectAdapter<TJoin>): Join<TJoin, TModel> =
        join(adapter, JoinType.NATURAL)

    /**
     * Adds a [JoinType.NATURAL] join on a specific table for this query.
     *
     * @param modelQueriable The query to join on.
     * @param <TJoin>        The class of the join table.
    </TJoin> */
    infix fun <TJoin : Any> naturalJoin(modelQueriable: ModelQueriable<TJoin>): Join<TJoin, TModel> =
        join(modelQueriable, JoinType.NATURAL)

    /**
     * Begins an INDEXED BY piece of this query with the specified name.
     *
     * @param indexProperty The index property generated.
     */
    infix fun indexedBy(indexProperty: IndexProperty<TModel>): IndexedBy<TModel> =
        IndexedBy(indexProperty, this)

}

/**
 * Extracts the [From] from a [ModelQueriable] if possible to get [From.associatedTables]
 */
@Suppress("UNCHECKED_CAST")
fun <T : Any> ModelQueriable<T>.extractFrom(): From<T>? {
    return if (this is From<*>) {
        this as From<T>
    } else if (this is Where<*> && whereBase is From<*>) {
        whereBase as From<T>
    } else {
        null
    }
}
