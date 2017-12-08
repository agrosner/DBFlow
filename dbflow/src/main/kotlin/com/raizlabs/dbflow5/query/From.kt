package com.raizlabs.dbflow5.query

import com.raizlabs.dbflow5.config.FlowManager
import com.raizlabs.dbflow5.database.DatabaseWrapper
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
 * @param querybase The base query we append this query to
 * @param table     The table this corresponds to
 */
internal constructor(
    databaseWrapper: DatabaseWrapper,

    /**
     * @return The base query, usually a [Delete], [Select], or [Update]
     */
    override val queryBuilderBase: Query, table: Class<TModel>)
    : BaseTransformable<TModel>(databaseWrapper, table) {

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

            queryBuilder.append(getTableAlias())

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

    private fun getTableAlias(): NameAlias {
        if (tableAlias == null) {
            tableAlias = NameAlias.Builder(FlowManager.getTableName(table)).build()
        }
        return tableAlias!!
    }

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
