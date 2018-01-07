package com.raizlabs.dbflow5.query

import com.raizlabs.dbflow5.config.FlowManager
import com.raizlabs.dbflow5.query.Join.JoinType
import com.raizlabs.dbflow5.query.property.IndexProperty
import com.raizlabs.dbflow5.sql.Query
import com.raizlabs.dbflow5.structure.ChangeAction
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
    table: Class<TModel>,

    /**
     * If specified, we use this as the subquery for the FROM statement.
     */
    private val modelQueriable: ModelQueriable<TModel>? = null)
    : BaseTransformable<TModel>(table) {

    /**
     * An alias for the table
     */
    private var tableAlias: NameAlias? = null

    /**
     * Enables the SQL JOIN statement
     */
    private val joins = arrayListOf<Join<*, *>>()

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
            table)
        from.joins.addAll(joins)
        from.tableAlias = tableAlias
        return from
    }

    /**
     * @return A list of [Class] that represents tables represented in this query. For every
     * [Join] on another table, this adds another [Class].
     */
    val associatedTables: KSet<Class<*>>
        get() {
            val tables = linkedSetOf<Class<*>>(table)
            joins.mapTo(tables) { it.table }
            return tables
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
    fun <TJoin : Any> join(table: Class<TJoin>, joinType: JoinType): Join<TJoin, TModel> {
        val join = Join(this, table, joinType)
        joins.add(join)
        return join
    }

    /**
     * Adds a join on a specific table for this query.
     *
     * @param modelQueriable A query we construct the [Join] from.
     * @param joinType       The type of join to use.
     */
    fun <TJoin : Any> join(modelQueriable: ModelQueriable<TJoin>, joinType: JoinType): Join<TJoin, TModel> {
        val join = Join(this, joinType, modelQueriable)
        joins.add(join)
        return join
    }

    /**
     * Adds a [JoinType.CROSS] join on a specific table for this query.
     *
     * @param table   The table to join on.
     * @param <TJoin> The class of the join table.
    </TJoin> */
    fun <TJoin : Any> crossJoin(table: Class<TJoin>): Join<TJoin, TModel> = join(table, JoinType.CROSS)

    /**
     * Adds a [JoinType.CROSS] join on a specific table for this query.
     *
     * @param modelQueriable The query to join on.
     * @param <TJoin>        The class of the join table.
    </TJoin> */
    fun <TJoin : Any> crossJoin(modelQueriable: ModelQueriable<TJoin>): Join<TJoin, TModel> =
        join(modelQueriable, JoinType.CROSS)

    /**
     * Adds a [JoinType.INNER] join on a specific table for this query.
     *
     * @param table   The table to join on.
     * @param <TJoin> The class of the join table.
    </TJoin> */
    fun <TJoin : Any> innerJoin(table: Class<TJoin>): Join<TJoin, TModel> = join(table, JoinType.INNER)

    /**
     * Adds a [JoinType.INNER] join on a specific table for this query.
     *
     * @param modelQueriable The query to join on.
     * @param <TJoin>        The class of the join table.
    </TJoin> */
    fun <TJoin : Any> innerJoin(modelQueriable: ModelQueriable<TJoin>): Join<TJoin, TModel> =
        join(modelQueriable, JoinType.INNER)

    /**
     * Adds a [JoinType.LEFT_OUTER] join on a specific table for this query.
     *
     * @param table   The table to join on.
     * @param <TJoin> The class of the join table.
    </TJoin> */
    fun <TJoin : Any> leftOuterJoin(table: Class<TJoin>): Join<TJoin, TModel> =
        join(table, JoinType.LEFT_OUTER)

    /**
     * Adds a [JoinType.LEFT_OUTER] join on a specific table for this query.
     *
     * @param modelQueriable The query to join on.
     * @param <TJoin>        The class of the join table.
    </TJoin> */
    fun <TJoin : Any> leftOuterJoin(modelQueriable: ModelQueriable<TJoin>): Join<TJoin, TModel> =
        join(modelQueriable, JoinType.LEFT_OUTER)


    /**
     * Adds a [JoinType.NATURAL] join on a specific table for this query.
     *
     * @param table   The table to join on.
     * @param <TJoin> The class of the join table.
    </TJoin> */
    fun <TJoin : Any> naturalJoin(table: Class<TJoin>): Join<TJoin, TModel> =
        join(table, JoinType.NATURAL)

    /**
     * Adds a [JoinType.NATURAL] join on a specific table for this query.
     *
     * @param modelQueriable The query to join on.
     * @param <TJoin>        The class of the join table.
    </TJoin> */
    fun <TJoin : Any> naturalJoin(modelQueriable: ModelQueriable<TJoin>): Join<TJoin, TModel> =
        join(modelQueriable, JoinType.NATURAL)

    /**
     * Begins an INDEXED BY piece of this query with the specified name.
     *
     * @param indexProperty The index property generated.
     */
    fun indexedBy(indexProperty: IndexProperty<TModel>): IndexedBy<TModel> =
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
