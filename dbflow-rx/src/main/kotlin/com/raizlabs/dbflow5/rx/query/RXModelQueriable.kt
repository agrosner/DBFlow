package com.raizlabs.dbflow5.rx.query

import com.raizlabs.dbflow5.config.databaseForTable
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.query.Join
import com.raizlabs.dbflow5.query.ModelQueriable
import com.raizlabs.dbflow5.query.list.FlowCursorList
import com.raizlabs.dbflow5.query.list.FlowQueryList
import com.raizlabs.dbflow5.structure.BaseQueryModel
import rx.Observable
import rx.Single

/**
 * Description: Mirrors [ModelQueriable] with RX constructs.
 */
interface RXModelQueriable<T : Any> : RXQueriable {

    fun queryList(databaseWrapper: DatabaseWrapper): Single<List<T>>

    /**
     * @return Single model, the first of potentially many results
     */
    fun querySingle(databaseWrapper: DatabaseWrapper): Single<T>

    /**
     * @return Queries for [.queryResults], and returns one at a time from this [Observable]
     */
    fun queryStreamResults(databaseWrapper: DatabaseWrapper): Observable<T>

    /**
     * @return A cursor-backed list that handles conversion, retrieval, and caching of lists. Can
     * cache models dynamically by setting [FlowCursorList.setCacheModels] to true.
     */
    fun cursorList(databaseWrapper: DatabaseWrapper): Single<FlowCursorList<T>>

    /**
     * @return A cursor-backed [List] that handles conversion, retrieval, caching, content changes,
     * and more.
     */
    fun flowQueryList(databaseWrapper: DatabaseWrapper): Single<FlowQueryList<T>>

    /**
     * Returns a [List] based on the custom [TQueryModel] you pass in.
     *
     * @param queryModelClass The query model class to use.
     * @param <TQueryModel>   The class that extends [BaseQueryModel]
     * @return A list of custom models that are not tied to a table.
    </TQueryModel> */
    fun <TQueryModel : Any> queryCustomList(queryModelClass: Class<TQueryModel>,
                                            databaseWrapper: DatabaseWrapper): Single<List<TQueryModel>>

    /**
     * Returns a single [TQueryModel] from this query.
     *
     * @param queryModelClass The class to use.
     * @param <TQueryModel>   The class that extends [BaseQueryModel]
     * @return A single model from the query.
    </TQueryModel> */
    fun <TQueryModel : Any> queryCustomSingle(queryModelClass: Class<TQueryModel>,
                                              databaseWrapper: DatabaseWrapper): Single<TQueryModel>

    /**
     * @return A new [Observable] that observes when the [T] table changes.
     * This can also be multiple tables, given if it results from a [Join] (one for each join table).
     */
    fun observeOnTableChanges(): Observable<ModelQueriable<T>>
}

inline val <reified T : Any> RXModelQueriable<T>.list
    get() = queryList(databaseForTable<T>())

inline val <reified T : Any> RXModelQueriable<T>.result
    get() = querySingle(databaseForTable<T>())

inline val <reified T : Any> RXModelQueriable<T>.streamResults
    get() = queryStreamResults(databaseForTable<T>())

inline val <T : Any> RXModelQueriable<T>.tableChanges
    get() = observeOnTableChanges()