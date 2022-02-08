package com.dbflow5.query

import com.dbflow5.adapter.RetrievalAdapter
import com.dbflow5.adapter.queriable.ListModelLoader
import com.dbflow5.config.FlowLog
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.sql.Query
import kotlinx.coroutines.runBlocking

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
        return runBlocking { adapter.loadList(databaseWrapper, query) }
    }

    override fun querySingle(databaseWrapper: DatabaseWrapper): TModel? {
        val query = query
        FlowLog.log(FlowLog.Level.V, "Executing query: $query")
        return runBlocking { adapter.loadSingle(databaseWrapper, query) }
    }

    override fun executeUpdateDelete(databaseWrapper: DatabaseWrapper): Long =
        compileStatement(databaseWrapper).use { it.executeUpdateDelete() }

    override fun <QueryClass : Any> queryCustomList(
        retrievalAdapter: RetrievalAdapter<QueryClass>,
        databaseWrapper: DatabaseWrapper
    )
        : List<QueryClass> {
        val query = query
        FlowLog.log(FlowLog.Level.V, "Executing query: $query")
        return runBlocking { retrievalAdapter.loadList(databaseWrapper, query) }
    }

    override fun <QueryClass : Any> queryCustomSingle(
        retrievalAdapter: RetrievalAdapter<QueryClass>,
        databaseWrapper: DatabaseWrapper
    )
        : QueryClass? {
        val query = query
        FlowLog.log(FlowLog.Level.V, "Executing query: $query")
        return runBlocking { retrievalAdapter.loadSingle(databaseWrapper, query) }
    }
}
