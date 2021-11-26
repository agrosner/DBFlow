package com.dbflow5.query

import com.dbflow5.appendQualifier
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.database.FlowCursor
import com.dbflow5.query.property.IProperty
import com.dbflow5.sql.QueryCloneable
import com.dbflow5.structure.ChangeAction

/**
 * Description: Defines the SQL WHERE statement of the query.
 */
class Where<T : Any>
/**
 * Constructs this class with the specified [com.dbflow5.config.FlowManager]
 * and [From] chunk
 *
 * @param whereBase The FROM or SET statement chunk
 */
internal constructor(
    /**
     * The first chunk of the SQL statement before this query.
     */
    val whereBase: WhereBase<T>, vararg conditions: SQLOperator
) : BaseModelQueriable<T>(whereBase.table),
    ModelQueriable<T>, Transformable<T>, QueryCloneable<Where<T>> {

    /**
     * Helps to build the where statement easily
     */
    private val operatorGroup: OperatorGroup = OperatorGroup.nonGroupingClause()

    private val groupByList = arrayListOf<NameAlias>()

    private val orderByList = arrayListOf<OrderBy>()

    /**
     * The SQL HAVING statement
     */
    private val havingGroup: OperatorGroup = OperatorGroup.nonGroupingClause()

    private var limit = VALUE_UNSET
    private var offset = VALUE_UNSET

    override val primaryAction: ChangeAction
        get() = whereBase.primaryAction

    override fun cloneSelf(): Where<T> {
        val where = Where(whereBase.cloneSelf(), *operatorGroup.conditions.toTypedArray())
        where.groupByList.addAll(groupByList)
        where.orderByList.addAll(orderByList)
        where.havingGroup.andAll(havingGroup.conditions)
        where.limit = limit
        where.offset = offset
        return where
    }

    override val query: String
        get() {
            val fromQuery = whereBase.query.trim { it <= ' ' }
            val queryBuilder = StringBuilder(fromQuery).append(" ")
                .appendQualifier("WHERE", operatorGroup.query)
                .appendQualifier("GROUP BY", groupByList.joinToString(separator = ","))
                .appendQualifier("HAVING", havingGroup.query)
                .appendQualifier("ORDER BY", orderByList.joinToString(separator = ","))

            if (limit > VALUE_UNSET) {
                queryBuilder.appendQualifier("LIMIT", limit.toString())
            }
            if (offset > VALUE_UNSET) {
                queryBuilder.appendQualifier("OFFSET", offset.toString())
            }

            return queryBuilder.toString()
        }

    init {
        operatorGroup.andAll(*conditions)
    }

    /**
     * Joins the [SQLOperator] by the prefix of "AND" (unless its the first condition).
     */
    infix fun and(condition: SQLOperator) = apply {
        operatorGroup.and(condition)
    }

    /**
     * Joins the [SQLOperator] by the prefix of "OR" (unless its the first condition).
     */
    infix fun or(condition: SQLOperator) = apply {
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
        orderByList.add(OrderBy.fromNameAlias(nameAlias, ascending))
    }

    override fun orderBy(property: IProperty<*>, ascending: Boolean) = apply {
        orderByList.add(OrderBy.fromProperty(property, ascending))
    }

    override fun orderBy(orderBy: OrderBy) = apply {
        orderByList.add(orderBy)
    }

    /**
     * Appends all ORDER BY here.
     *
     * @param orderByList The order by.
     * @return this instance.
     */
    override fun orderByAll(orderByList: List<OrderBy>) = apply {
        this.orderByList.addAll(orderByList)
    }

    override fun limit(count: Long) = apply {
        this.limit = count
    }

    override fun offset(offset: Long) = apply {
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
     * @return the result of the query as a [FlowCursor].
     */
    override fun cursor(databaseWrapper: DatabaseWrapper): FlowCursor? =// Query the sql here
        when {
            whereBase.queryBuilderBase is Select -> databaseWrapper.rawQuery(query, null)
            else -> super.cursor(databaseWrapper)
        }

    /**
     * Queries for all of the results this statement returns from a DB cursor in the form of the [T]
     *
     * @return All of the entries in the DB converted into [T]
     */
    override fun queryList(databaseWrapper: DatabaseWrapper): MutableList<T> {
        checkSelect("cursor")
        return super.queryList(databaseWrapper)
    }

    /**
     * Queries and returns only the first [T] result from the DB. Will enforce a limit of 1 item
     * returned from the database.
     *
     * @return The first result of this query. Note: this cursor forces a limit of 1 from the database.
     */
    override fun querySingle(databaseWrapper: DatabaseWrapper): T? {
        checkSelect("cursor")
        limit(1)
        return super.querySingle(databaseWrapper)
    }

    private fun checkSelect(methodName: String) {
        if (whereBase.queryBuilderBase !is Select) {
            throw IllegalArgumentException("Please use $methodName(). The beginning is not a ISelect")
        }
    }

    companion object {

        private val VALUE_UNSET = -1L
    }
}
