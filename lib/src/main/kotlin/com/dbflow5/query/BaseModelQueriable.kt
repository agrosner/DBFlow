package com.dbflow5.query

import android.os.Handler
import com.dbflow5.adapter.RetrievalAdapter
import com.dbflow5.adapter.queriable.ListModelLoader
import com.dbflow5.adapter.queriable.SingleModelLoader
import com.dbflow5.config.FlowLog
import com.dbflow5.config.FlowManager
import com.dbflow5.config.retrievalAdapter
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.query.list.FlowCursorList
import com.dbflow5.query.list.FlowQueryList
import com.dbflow5.sql.Query

/**
 * Description: Provides a base implementation of [ModelQueriable] to simplify a lot of code. It provides the
 * default implementation for convenience.
 */
abstract class BaseModelQueriable<TModel : Any>
/**
 * Constructs new instance of this class and is meant for subclasses only.
 *
 * @param table the table that belongs to this query.
 */
protected constructor(table: Class<TModel>) : BaseQueriable<TModel>(table), ModelQueriable<TModel>,
    Query {

    private val retrievalAdapter: RetrievalAdapter<TModel> by lazy {
        FlowManager.getRetrievalAdapter(
            table
        )
    }

    private var _cacheListModelLoader: ListModelLoader<TModel>? = null
    protected val listModelLoader: ListModelLoader<TModel>
        get() = retrievalAdapter.nonCacheableListModelLoader

    protected val singleModelLoader: SingleModelLoader<TModel>
        get() = retrievalAdapter.nonCacheableSingleModelLoader

    override fun queryList(databaseWrapper: DatabaseWrapper): MutableList<TModel> {
        val query = query
        FlowLog.log(FlowLog.Level.V, "Executing query: $query")
        return listModelLoader.load(databaseWrapper, query)!!
    }

    override fun querySingle(databaseWrapper: DatabaseWrapper): TModel? {
        val query = query
        FlowLog.log(FlowLog.Level.V, "Executing query: $query")
        return singleModelLoader.load(databaseWrapper, query)
    }

    override fun cursorList(databaseWrapper: DatabaseWrapper): FlowCursorList<TModel> =
        FlowCursorList.Builder(modelQueriable = this, databaseWrapper = databaseWrapper).build()

    override fun flowQueryList(databaseWrapper: DatabaseWrapper): FlowQueryList<TModel> =
        FlowQueryList.Builder(modelQueriable = this, databaseWrapper = databaseWrapper).build()

    override fun executeUpdateDelete(databaseWrapper: DatabaseWrapper): Long =
        compileStatement(databaseWrapper).use { it.executeUpdateDelete() }

    override fun <QueryClass : Any> queryCustomList(
        queryModelClass: Class<QueryClass>,
        databaseWrapper: DatabaseWrapper
    )
        : MutableList<QueryClass> {
        val query = query
        FlowLog.log(FlowLog.Level.V, "Executing query: $query")
        return getListQueryModelLoader(queryModelClass).load(databaseWrapper, query)!!
    }

    override fun <QueryClass : Any> queryCustomSingle(
        queryModelClass: Class<QueryClass>,
        databaseWrapper: DatabaseWrapper
    )
        : QueryClass? {
        val query = query
        FlowLog.log(FlowLog.Level.V, "Executing query: $query")
        return getSingleQueryModelLoader(queryModelClass).load(databaseWrapper, query)
    }


    protected fun <T : Any> getListQueryModelLoader(table: Class<T>): ListModelLoader<T> =
        table.retrievalAdapter.nonCacheableListModelLoader

    protected fun <T : Any> getSingleQueryModelLoader(table: Class<T>): SingleModelLoader<T> =
        table.retrievalAdapter.nonCacheableSingleModelLoader
}

/**
 * Constructs a flowQueryList allowing a custom [Handler].
 */
fun <T : Any> ModelQueriable<T>.flowQueryList(
    databaseWrapper: DatabaseWrapper,
    refreshHandler: Handler
) =
    FlowQueryList.Builder(
        modelQueriable = this, databaseWrapper = databaseWrapper,
        refreshHandler = refreshHandler
    ).build()
