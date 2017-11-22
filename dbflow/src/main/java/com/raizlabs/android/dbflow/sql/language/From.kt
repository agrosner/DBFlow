package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.sql.Query
import com.raizlabs.android.dbflow.sql.QueryBuilder
import com.raizlabs.android.dbflow.sql.language.Join.JoinType
import com.raizlabs.android.dbflow.sql.language.property.IndexProperty
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable
import com.raizlabs.android.dbflow.structure.BaseModel
import java.util.*
import kotlin.collections.Set as KSet

/**
 * Description: The SQL FROM query wrapper that must have a [Query] base.
 */
class From<TModel>
/**
 * The SQL from statement constructed.
 *
 * @param querybase The base query we append this query to
 * @param table     The table this corresponds to
 */
(
        /**
         * The base such as [Delete], [Select] and more!
         */
        /**
         * @return The base query, usually a [Delete], [Select], or [Update]
         */
        override val queryBuilderBase: Query, table: Class<TModel>) : BaseTransformable<TModel>(table) {

    /**
     * An alias for the table
     */
    private var tableAlias: NameAlias? = null

    /**
     * Enables the SQL JOIN statement
     */
    private val joins = ArrayList<Join<*, *>>()

    override val primaryAction: BaseModel.Action
        get() = if (queryBuilderBase is Delete) BaseModel.Action.DELETE else BaseModel.Action.CHANGE

    override val query: String
        get() {
            val queryBuilder = QueryBuilder()
                    .append(queryBuilderBase.query)
            if (queryBuilderBase !is Update<*>) {
                queryBuilder.append("FROM ")
            }

            queryBuilder.append(getTableAlias())

            if (queryBuilderBase is Select) {
                if (!joins.isEmpty()) {
                    queryBuilder.appendSpace()
                }
                for (join in joins) {
                    queryBuilder.append(join.query)
                }
            } else {
                queryBuilder.appendSpace()
            }

            return queryBuilder.query
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
    fun `as`(alias: String): From<TModel> {
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
    fun <TJoin> join(table: Class<TJoin>, joinType: JoinType): Join<TJoin, TModel> {
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
    fun <TJoin> join(modelQueriable: ModelQueriable<TJoin>, joinType: JoinType): Join<TJoin, TModel> {
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
    fun <TJoin> crossJoin(table: Class<TJoin>): Join<TJoin, TModel> = join(table, JoinType.CROSS)

    /**
     * Adds a [JoinType.CROSS] join on a specific table for this query.
     *
     * @param modelQueriable The query to join on.
     * @param <TJoin>        The class of the join table.
    </TJoin> */
    fun <TJoin> crossJoin(modelQueriable: ModelQueriable<TJoin>): Join<TJoin, TModel> =
            join(modelQueriable, JoinType.CROSS)

    /**
     * Adds a [JoinType.INNER] join on a specific table for this query.
     *
     * @param table   The table to join on.
     * @param <TJoin> The class of the join table.
    </TJoin> */
    fun <TJoin> innerJoin(table: Class<TJoin>): Join<TJoin, TModel> = join(table, JoinType.INNER)

    /**
     * Adds a [JoinType.INNER] join on a specific table for this query.
     *
     * @param modelQueriable The query to join on.
     * @param <TJoin>        The class of the join table.
    </TJoin> */
    fun <TJoin> innerJoin(modelQueriable: ModelQueriable<TJoin>): Join<TJoin, TModel> =
            join(modelQueriable, JoinType.INNER)

    /**
     * Adds a [JoinType.LEFT_OUTER] join on a specific table for this query.
     *
     * @param table   The table to join on.
     * @param <TJoin> The class of the join table.
    </TJoin> */
    fun <TJoin> leftOuterJoin(table: Class<TJoin>): Join<TJoin, TModel> =
            join(table, JoinType.LEFT_OUTER)

    /**
     * Adds a [JoinType.LEFT_OUTER] join on a specific table for this query.
     *
     * @param modelQueriable The query to join on.
     * @param <TJoin>        The class of the join table.
    </TJoin> */
    fun <TJoin> leftOuterJoin(modelQueriable: ModelQueriable<TJoin>): Join<TJoin, TModel> =
            join(modelQueriable, JoinType.LEFT_OUTER)


    /**
     * Adds a [JoinType.NATURAL] join on a specific table for this query.
     *
     * @param table   The table to join on.
     * @param <TJoin> The class of the join table.
    </TJoin> */
    fun <TJoin> naturalJoin(table: Class<TJoin>): Join<TJoin, TModel> =
            join(table, JoinType.NATURAL)

    /**
     * Adds a [JoinType.NATURAL] join on a specific table for this query.
     *
     * @param modelQueriable The query to join on.
     * @param <TJoin>        The class of the join table.
    </TJoin> */
    fun <TJoin> naturalJoin(modelQueriable: ModelQueriable<TJoin>): Join<TJoin, TModel> =
            join(modelQueriable, JoinType.NATURAL)

    /**
     * Begins an INDEXED BY piece of this query with the specified name.
     *
     * @param indexProperty The index property generated.
     */
    fun indexedBy(indexProperty: IndexProperty<TModel>): IndexedBy<TModel> =
            IndexedBy(indexProperty, this)
}
