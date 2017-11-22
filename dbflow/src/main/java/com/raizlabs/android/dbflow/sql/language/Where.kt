package com.raizlabs.android.dbflow.sql.language

import android.database.Cursor
import com.raizlabs.android.dbflow.annotation.provider.ContentProvider
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.sql.QueryBuilder
import com.raizlabs.android.dbflow.sql.language.property.IProperty
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper
import com.raizlabs.android.dbflow.structure.database.FlowCursor
import java.util.*

/**
 * Description: Defines the SQL WHERE statement of the query.
 */
class Where<TModel>
/**
 * Constructs this class with the specified [com.raizlabs.android.dbflow.config.FlowManager]
 * and [From] chunk
 *
 * @param whereBase The FROM or SET statement chunk
 */
(
        /**
         * The first chunk of the SQL statement before this query.
         */
        val whereBase: WhereBase<TModel>, vararg conditions: SQLOperator)
    : BaseModelQueriable<TModel>(whereBase.table), ModelQueriable<TModel>, Transformable<TModel> {

    /**
     * Helps to build the where statement easily
     */
    private val operatorGroup: OperatorGroup = OperatorGroup.nonGroupingClause()

    private val groupByList = ArrayList<NameAlias>()

    private val orderByList = ArrayList<OrderBy>()

    /**
     * The SQL HAVING statement
     */
    private val havingGroup: OperatorGroup = OperatorGroup.nonGroupingClause()

    private var limit = VALUE_UNSET
    private var offset = VALUE_UNSET

    override val primaryAction: BaseModel.Action
        get() = whereBase.primaryAction

    override val query: String
        get() {
            val fromQuery = whereBase.query.trim { it <= ' ' }
            val queryBuilder = QueryBuilder().append(fromQuery).appendSpace()
                    .appendQualifier("WHERE", operatorGroup.query)
                    .appendQualifier("GROUP BY", QueryBuilder.join(",", groupByList))
                    .appendQualifier("HAVING", havingGroup.query)
                    .appendQualifier("ORDER BY", QueryBuilder.join(",", orderByList))

            if (limit > VALUE_UNSET) {
                queryBuilder.appendQualifier("LIMIT", limit.toString())
            }
            if (offset > VALUE_UNSET) {
                queryBuilder.appendQualifier("OFFSET", offset.toString())
            }

            return queryBuilder.query
        }

    init {
        operatorGroup.andAll(*conditions)
    }

    /**
     * Joins the [SQLOperator] by the prefix of "AND" (unless its the first condition).
     */
    fun and(condition: SQLOperator) = apply {
        operatorGroup.and(condition)
    }

    /**
     * Joins the [SQLOperator] by the prefix of "OR" (unless its the first condition).
     */
    fun or(condition: SQLOperator) = apply {
        operatorGroup.or(condition)
    }

    /**
     * Joins all of the [SQLOperator] by the prefix of "AND" (unless its the first condition).
     */
    fun andAll(conditions: List<SQLOperator>) = apply {
        operatorGroup.andAll(conditions)
    }

    /**
     * Joins all of the [SQLOperator] by the prefix of "AND" (unless its the first condition).
     */
    fun andAll(vararg conditions: SQLOperator) = apply {
        operatorGroup.andAll(*conditions)
    }

    override fun groupBy(vararg nameAliases: NameAlias) = apply {
        groupByList.addAll(nameAliases.toList())
    }

    override fun groupBy(vararg properties: IProperty<*>) = apply {
        properties.mapTo(groupByList) { it.nameAlias }
    }

    /**
     * Defines a SQL HAVING statement without the HAVING.
     *
     * @param conditions The array of [SQLOperator]
     * @return
     */
    override fun having(vararg conditions: SQLOperator) = apply {
        havingGroup.andAll(*conditions)
    }

    override fun orderBy(nameAlias: NameAlias, ascending: Boolean) = apply {
        orderByList.add(OrderBy(nameAlias, ascending))
    }

    override fun orderBy(property: IProperty<*>, ascending: Boolean) = apply {
        orderByList.add(OrderBy(property.nameAlias, ascending))
    }

    override fun orderBy(orderBy: OrderBy) = apply {
        orderByList.add(orderBy)
    }

    /**
     * For use in [ContentProvider] generation. Appends all ORDER BY here.
     *
     * @param orderBies The order by.
     * @return this instance.
     */
    override fun orderByAll(orderBies: List<OrderBy>) = apply {
        orderByList.addAll(orderBies)
    }

    override fun limit(count: Int) = apply {
        this.limit = count
    }

    override fun offset(offset: Int) = apply {
        this.offset = offset
    }

    /**
     * Specify that we use an EXISTS statement for this Where class.
     *
     * @param where The query to use in the EXISTS clause. Such as SELECT * FROM `MyTable` WHERE ... etc.
     * @return This where with an EXISTS clause.
     */
    fun exists(where: Where<*>) = apply {
        operatorGroup.and(ExistenceOperator(where))
    }

    /**
     * @return the result of the query as a [Cursor].
     */
    override fun query(databaseWrapper: DatabaseWrapper): FlowCursor? =// Query the sql here
            when {
                whereBase.queryBuilderBase is Select -> databaseWrapper.rawQuery(query, null)
                else -> super.query(databaseWrapper)
            }

    override fun query(): FlowCursor? = query(FlowManager.getDatabaseForTable(table).writableDatabase)

    /**
     * Queries for all of the results this statement returns from a DB cursor in the form of the [TModel]
     *
     * @return All of the entries in the DB converted into [TModel]
     */
    override fun queryList(): MutableList<TModel> {
        checkSelect("query")
        return super.queryList()
    }

    /**
     * Queries and returns only the first [TModel] result from the DB. Will enforce a limit of 1 item
     * returned from the database.
     *
     * @return The first result of this query. Note: this query forces a limit of 1 from the database.
     */
    override fun querySingle(): TModel? {
        checkSelect("query")
        limit(1)
        return super.querySingle()
    }

    private fun checkSelect(methodName: String) {
        if (whereBase.queryBuilderBase !is Select) {
            throw IllegalArgumentException("Please use $methodName(). The beginning is not a ISelect")
        }
    }

    companion object {

        private val VALUE_UNSET = -1
    }
}
