package com.raizlabs.dbflow5.rx.query

import com.raizlabs.dbflow5.query.list.FlowCursorList
import com.raizlabs.dbflow5.query.list.FlowQueryList
import com.raizlabs.dbflow5.query.CursorResult
import com.raizlabs.dbflow5.query.Join
import com.raizlabs.dbflow5.query.ModelQueriable
import com.raizlabs.dbflow5.structure.BaseQueryModel
import rx.Observable
import rx.Single

/**
 * Description: Mirrors [ModelQueriable] with RX constructs.
 */
interface RXModelQueriable<T : Any> : RXQueriable {

    fun queryResults(): Single<CursorResult<T>>

    fun queryList(): Single<List<T>>

    /**
     * @return Single model, the first of potentially many results
     */
    fun querySingle(): Single<T>

    /**
     * @return Queries for [.queryResults], and returns one at a time from this [Observable]
     */
    fun queryStreamResults(): Observable<T>

    /**
     * @return A cursor-backed list that handles conversion, retrieval, and caching of lists. Can
     * cache models dynamically by setting [FlowCursorList.setCacheModels] to true.
     */
    fun cursorList(): Single<FlowCursorList<T>>

    /**
     * @return A cursor-backed [List] that handles conversion, retrieval, caching, content changes,
     * and more.
     */
    fun flowQueryList(): Single<FlowQueryList<T>>

    /**
     * Returns a [List] based on the custom [TQueryModel] you pass in.
     *
     * @param queryModelClass The query model class to use.
     * @param <TQueryModel>   The class that extends [BaseQueryModel]
     * @return A list of custom models that are not tied to a table.
    </TQueryModel> */
    fun <TQueryModel : Any> queryCustomList(queryModelClass: Class<TQueryModel>): Single<List<TQueryModel>>

    /**
     * Returns a single [TQueryModel] from this query.
     *
     * @param queryModelClass The class to use.
     * @param <TQueryModel>   The class that extends [BaseQueryModel]
     * @return A single model from the query.
    </TQueryModel> */
    fun <TQueryModel : Any> queryCustomSingle(queryModelClass: Class<TQueryModel>): Single<TQueryModel>

    /**
     * @return A new [Observable] that observes when the [T] table changes.
     * This can also be multiple tables, given if it results from a [Join] (one for each join table).
     */
    fun observeOnTableChanges(): Observable<ModelQueriable<T>>
}

inline val <T : Any> RXModelQueriable<T>.list
    get() = queryList()

inline val <T : Any> RXModelQueriable<T>.result
    get() = querySingle()

inline val <T : Any> RXModelQueriable<T>.cursorResult
    get() = queryResults()

inline val <T : Any> RXModelQueriable<T>.streamResults
    get() = queryStreamResults()

inline val <T : Any> RXModelQueriable<T>.tableChanges
    get() = observeOnTableChanges()