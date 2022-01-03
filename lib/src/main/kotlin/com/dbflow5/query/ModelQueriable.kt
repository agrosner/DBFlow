package com.dbflow5.query

import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.database.SQLiteException


typealias ModelQueriableEvalFn<T, R> = suspend ModelQueriable<T>.(DatabaseWrapper) -> R

/**
 * Description: An interface for query objects to enable you to cursor from the database in a structured way.
 * Examples of such statements are: [From], [Where], [StringQuery]
 */
interface ModelQueriable<T : Any> : Queriable {

    /**
     * @return the table that this query comes from.
     */
    val table: Class<T>

    /**
     * @return a list of model converted items
     */
    suspend fun queryList(databaseWrapper: DatabaseWrapper): MutableList<T>

    /**
     * @return Single model, the first of potentially many results
     */
    suspend fun querySingle(databaseWrapper: DatabaseWrapper): T?

    /**
     * A non-null result. throws a [SQLiteException] if the query reaches no result.
     */
    suspend fun requireSingle(db: DatabaseWrapper) = querySingle(db)
        ?: throw SQLiteException("Model result not found for $this")

    /**
     * Returns a [List] based on the custom [TQuery] you pass in.
     *
     * @param queryModelClass The query model class to use.
     * @return A list of custom models that are not tied to a table.
     */
    suspend fun <TQuery : Any> queryCustomList(
        queryModelClass: Class<TQuery>,
        databaseWrapper: DatabaseWrapper
    ): MutableList<TQuery>

    /**
     * Returns a single [TQueryModel] from this query.
     *
     * @param queryModelClass The class to use.
     * @return A single model from the query.
     */
    suspend fun <TQueryModel : Any> queryCustomSingle(
        queryModelClass: Class<TQueryModel>,
        databaseWrapper: DatabaseWrapper
    ): TQueryModel?

    /**
     * Attempt to constrain this [ModelQueriable] if it supports it via [Transformable] methods. Otherwise,
     * we just return itself.
     */
    @Suppress("UNCHECKED_CAST")
    fun attemptConstrain(offset: Long, limit: Long): ModelQueriable<T> {
        return when {
            this is Transformable<*> -> (this as Transformable<T>).constrain(offset, limit)
            else -> this
        }
    }

}

/**
 * Trims and wraps a [ModelQueriable.query] in parenthesis.
 * E.G. wraps: select * from table into (select * from table)
 */
internal inline val <T : Any> ModelQueriable<T>.enclosedQuery
    get() = "(${query.trim { it <= ' ' }})"

suspend inline fun <reified T : Any> ModelQueriable<*>.queryCustomList(db: DatabaseWrapper) =
    queryCustomList(T::class.java, db)

suspend inline fun <reified T : Any> ModelQueriable<*>.queryCustomSingle(db: DatabaseWrapper) =
    queryCustomSingle(T::class.java, db)

suspend inline fun <reified T : Any> ModelQueriable<*>.requireCustomSingle(db: DatabaseWrapper) =
    queryCustomSingle(T::class.java, db)
        ?: throw SQLiteException("QueryModel result not found for $this")
