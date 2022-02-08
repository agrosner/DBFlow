package com.dbflow5.query

import com.dbflow5.adapter.RetrievalAdapter
import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.config.beginTransactionAsync
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.database.SQLiteException
import com.dbflow5.sql.Query


typealias ModelQueriableEvalFn<T, R> = ModelQueriable<T>.(DatabaseWrapper) -> R

/**
 * Description: An interface for query objects to enable you to cursor from the database in a structured way.
 * Examples of such statements are: [From], [Where], [StringQuery]
 */
interface ModelQueriable<T : Any> : Queriable {

    val adapter: RetrievalAdapter<T>

    /**
     * @return a list of model converted items
     */
    fun queryList(databaseWrapper: DatabaseWrapper): List<T>

    /**
     * @return Single model, the first of potentially many results
     */
    fun querySingle(databaseWrapper: DatabaseWrapper): T?

    /**
     * A non-null result. throws a [SQLiteException] if the query reaches no result.
     */
    fun requireSingle(db: DatabaseWrapper) = querySingle(db)
        ?: throw SQLiteException("Model result not found for $this")

    /**
     * Returns a [List] based on the custom [TQuery] you pass in.
     *
     * @param retrievalAdapter - the associated [RetrievalAdapter] from DB.
     * @return A list of custom models that are not tied to a table.
     */
    fun <TQuery : Any> queryCustomList(
        retrievalAdapter: RetrievalAdapter<TQuery>,
        databaseWrapper: DatabaseWrapper
    ): List<TQuery>

    /**
     * Returns a single [TQueryModel] from this query.
     *
     * @param retrievalAdapter - the generated [RetrievalAdapter] from DB to use.
     * @return A single model from the query.
     */
    fun <TQueryModel : Any> queryCustomSingle(
        retrievalAdapter: RetrievalAdapter<TQueryModel>,
        databaseWrapper: DatabaseWrapper
    ): TQueryModel?

    /**
     * Returns a single [TQueryModel] from this query.
     *
     * @param queryModelClass The class to use.
     * @return A single model from the query.
     */
    fun <TQueryModel : Any> requireCustomSingle(
        retrievalAdapter: RetrievalAdapter<TQueryModel>,
        databaseWrapper: DatabaseWrapper
    ) = queryCustomSingle(retrievalAdapter, databaseWrapper)
        ?: throw SQLiteException("QueryModel result not found for $this")

    /**
     * Begins an async DB transaction using the specified TransactionManager.
     */
    fun <DB : DBFlowDatabase, R : Any?> async(
        databaseWrapper: DB,
        modelQueriableFn: ModelQueriable<T>.(DatabaseWrapper) -> R
    ) =
        databaseWrapper.beginTransactionAsync { modelQueriableFn(db) }

    /**
     * Attempt to constrain this [ModelQueriable] if it supports it via [Transformable] methods. Otherwise,
     * we just return itself.
     */
    @Suppress("UNCHECKED_CAST")
    fun attemptConstrain(offset: Long, limit: Long): ModelQueriable<T> {
        return when (this) {
            is Transformable<*> -> (this as Transformable<T>).constrain(offset, limit)
            else -> this
        }
    }

}

/**
 * Trims and wraps a [ModelQueriable.query] in parenthesis.
 * E.G. wraps: select * from table into (select * from table)
 */
internal inline val Query.enclosedQuery
    get() = "(${query.trim { it <= ' ' }})"
