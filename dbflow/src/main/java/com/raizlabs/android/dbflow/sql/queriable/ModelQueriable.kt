package com.raizlabs.android.dbflow.sql.queriable

import android.database.Cursor

import com.raizlabs.android.dbflow.list.FlowCursorList
import com.raizlabs.android.dbflow.list.FlowQueryList
import com.raizlabs.android.dbflow.sql.language.CursorResult
import com.raizlabs.android.dbflow.sql.language.From
import com.raizlabs.android.dbflow.sql.language.Where
import com.raizlabs.android.dbflow.structure.BaseQueryModel
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper

/**
 * Description: An interface for query objects to enable you to query from the database in a structured way.
 * Examples of such statements are: [From], [Where], [StringQuery]
 */
interface ModelQueriable<T : Any> : Queriable {

    /**
     * @return the table that this query comes from.
     */
    val table: Class<T>

    /**
     * @return A wrapper class around [Cursor] that allows you to convert its data into results.
     */
    fun queryResults(): CursorResult<T>

    /**
     * @return a list of model converted items
     */
    fun queryList(): MutableList<T>

    /**
     * @return Single model, the first of potentially many results
     */
    fun querySingle(): T?

    /**
     * @return A cursor-backed list that handles conversion, retrieval, and caching of lists. Can
     * cache models dynamically by setting [FlowCursorList.setCacheModels] to true.
     */
    fun cursorList(): FlowCursorList<T>

    /**
     * @return A cursor-backed [List] that handles conversion, retrieval, caching, content changes,
     * and more.
     */
    fun flowQueryList(): FlowQueryList<T>

    /**
     * @return an async version of this query to run.
     */
    fun async(): AsyncQuery<T>

    /**
     * Returns a [List] based on the custom [TQueryModel] you pass in.
     *
     * @param queryModelClass The query model class to use.
     * @param <TQueryModel>   The class that extends [BaseQueryModel]
     * @return A list of custom models that are not tied to a table.
    </TQueryModel> */
    fun <TQueryModel : Any> queryCustomList(queryModelClass: Class<TQueryModel>): MutableList<TQueryModel>

    /**
     * Returns a single [TQueryModel] from this query.
     *
     * @param queryModelClass The class to use.
     * @param <TQueryModel>   The class that extends [BaseQueryModel]
     * @return A single model from the query.
    </TQueryModel> */
    fun <TQueryModel : Any> queryCustomSingle(queryModelClass: Class<TQueryModel>): TQueryModel?

}

inline val <T : Any> ModelQueriable<T>.list
    get() = queryList()

inline val <T : Any> ModelQueriable<T>.result
    get() = querySingle()

inline val <T : Any> ModelQueriable<T>.cursorResult
    get() = queryResults()

inline val <T : Any> ModelQueriable<T>.flowQueryList
    get() = flowQueryList()

inline val <T : Any> ModelQueriable<T>.cursorList
    get() = cursorList()

inline val <T : Any> ModelQueriable<T>.async
    get() = async()