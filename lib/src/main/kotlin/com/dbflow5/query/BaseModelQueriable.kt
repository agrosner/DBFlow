package com.dbflow5.query

import android.os.Handler
import com.dbflow5.adapter.RetrievalAdapter
import com.dbflow5.adapter.queriable.ListModelLoader
import com.dbflow5.config.FlowLog
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
protected constructor(adapter: RetrievalAdapter<TModel>) :
    BaseQueriable<TModel>(adapter), ModelQueriable<TModel>,
    Query {

    private var _cacheListModelLoader: ListModelLoader<TModel>? = null

    override fun queryList(databaseWrapper: DatabaseWrapper): List<TModel> {
        val query = query
        FlowLog.log(FlowLog.Level.V, "Executing query: $query")
        return adapter.loadList(databaseWrapper, query)!!
    }

    override fun querySingle(databaseWrapper: DatabaseWrapper): TModel? {
        val query = query
        FlowLog.log(FlowLog.Level.V, "Executing query: $query")
        return adapter.loadSingle(databaseWrapper, query)
    }

    override fun cursorList(databaseWrapper: DatabaseWrapper): FlowCursorList<TModel> =
        FlowCursorList.Builder(modelQueriable = this, databaseWrapper = databaseWrapper).build()

    override fun flowQueryList(databaseWrapper: DatabaseWrapper): FlowQueryList<TModel> =
        FlowQueryList.Builder(modelQueriable = this, databaseWrapper = databaseWrapper).build()

    override fun executeUpdateDelete(databaseWrapper: DatabaseWrapper): Long =
        compileStatement(databaseWrapper).use { it.executeUpdateDelete() }

    override fun <QueryClass : Any> queryCustomList(
        retrievalAdapter: RetrievalAdapter<QueryClass>,
        databaseWrapper: DatabaseWrapper
    )
        : List<QueryClass> {
        val query = query
        FlowLog.log(FlowLog.Level.V, "Executing query: $query")
        return retrievalAdapter.loadList(databaseWrapper, query)!!
    }

    override fun <QueryClass : Any> queryCustomSingle(
        retrievalAdapter: RetrievalAdapter<QueryClass>,
        databaseWrapper: DatabaseWrapper
    )
        : QueryClass? {
        val query = query
        FlowLog.log(FlowLog.Level.V, "Executing query: $query")
        return retrievalAdapter.loadSingle(databaseWrapper, query)
    }
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
