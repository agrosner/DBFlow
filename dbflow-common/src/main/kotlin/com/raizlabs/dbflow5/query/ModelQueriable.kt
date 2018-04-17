package com.raizlabs.dbflow5.query

import com.raizlabs.dbflow5.KClass
import com.raizlabs.dbflow5.config.DBFlowDatabase
import com.raizlabs.dbflow5.config.databaseForTable
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.query.list.FlowCursorList
import com.raizlabs.dbflow5.query.list.FlowQueryList
import com.raizlabs.dbflow5.structure.BaseQueryModel


/**
 * Description: An interface for query objects to enable you to cursor from the database in a structured way.
 * Examples of such statements are: [From], [Where], [StringQuery]
 */
interface ModelQueriable<T : Any> : Queriable {

    /**
     * @return the table that this query comes from.
     */
    val table: KClass<T>

    /**
     * @return a list of model converted items
     */
    fun queryList(databaseWrapper: DatabaseWrapper): MutableList<T>

    /**
     * @return Single model, the first of potentially many results
     */
    fun querySingle(databaseWrapper: DatabaseWrapper): T?

    /**
     * @return A cursor-backed list that handles conversion, retrieval, and caching of lists. Can
     * cache models dynamically by setting [FlowCursorList.setCacheModels] to true.
     */
    fun cursorList(databaseWrapper: DatabaseWrapper): FlowCursorList<T>

    /**
     * @return A cursor-backed [List] that handles conversion, retrieval, caching, content changes,
     * and more.
     */
    fun flowQueryList(databaseWrapper: DatabaseWrapper): FlowQueryList<T>

    /**
     * Returns a [List] based on the custom [TQueryModel] you pass in.
     *
     * @param queryModelClass The query model class to use.
     * @param <TQueryModel>   The class that extends [BaseQueryModel]
     * @return A list of custom models that are not tied to a table.
    </TQueryModel> */
    fun <TQueryModel : Any> queryCustomList(queryModelClass: KClass<TQueryModel>,
                                            databaseWrapper: DatabaseWrapper): MutableList<TQueryModel>

    /**
     * Returns a single [TQueryModel] from this query.
     *
     * @param queryModelClass The class to use.
     * @param <TQueryModel>   The class that extends [BaseQueryModel]
     * @return A single model from the query.
    </TQueryModel> */
    fun <TQueryModel : Any> queryCustomSingle(queryModelClass: KClass<TQueryModel>,
                                              databaseWrapper: DatabaseWrapper): TQueryModel?

    /**
     * Disables caching on this query for the object retrieved from DB (if caching enabled). If
     * caching is not enabled, this method is ignored. This also disables caching in a [FlowCursorList]
     * or [FlowQueryList] if you [.flowQueryList] or [.cursorList]
     */
    fun disableCaching(): ModelQueriable<T>

    fun <R : Any?> async(databaseWrapper: DBFlowDatabase,
                         modelQueriableFn: ModelQueriable<T>.(DatabaseWrapper) -> R) =
        databaseWrapper.beginTransactionAsync { modelQueriableFn(it) }

}

internal inline val <T : Any> ModelQueriable<T>.enclosedQuery
    get() = "(${query.trim({ it <= ' ' })})"

inline val <reified T : Any> ModelQueriable<T>.list
    get() = queryList(databaseForTable<T>())

inline val <reified T : Any> ModelQueriable<T>.result
    get() = querySingle(databaseForTable<T>())

inline val <reified T : Any> ModelQueriable<T>.requireResult
    get() = result!!

inline val <reified T : Any> ModelQueriable<T>.flowQueryList
    get() = flowQueryList(databaseForTable<T>())

inline val <reified T : Any> ModelQueriable<T>.cursorList
    get() = cursorList(databaseForTable<T>())
