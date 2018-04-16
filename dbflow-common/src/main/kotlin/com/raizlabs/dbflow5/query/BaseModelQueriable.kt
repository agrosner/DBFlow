package com.raizlabs.dbflow5.query

import com.raizlabs.dbflow5.KClass
import com.raizlabs.dbflow5.adapter.RetrievalAdapter
import com.raizlabs.dbflow5.adapter.queriable.ListModelLoader
import com.raizlabs.dbflow5.adapter.queriable.SingleModelLoader
import com.raizlabs.dbflow5.config.FlowLog
import com.raizlabs.dbflow5.config.FlowManager
import com.raizlabs.dbflow5.config.queryModelAdapter
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.query.list.FlowCursorList
import com.raizlabs.dbflow5.query.list.FlowQueryList
import com.raizlabs.dbflow5.use

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
protected constructor(table: KClass<TModel>)
    : BaseQueriable<TModel>(table), ModelQueriable<TModel>, Query {

    private val retrievalAdapter: RetrievalAdapter<TModel> by lazy { FlowManager.getRetrievalAdapter(table) }

    private var cachingEnabled = true

    protected val listModelLoader: ListModelLoader<TModel>
        get() = if (cachingEnabled) {
            retrievalAdapter.listModelLoader
        } else {
            retrievalAdapter.nonCacheableListModelLoader
        }

    protected val singleModelLoader: SingleModelLoader<TModel>
        get() = if (cachingEnabled) {
            retrievalAdapter.singleModelLoader
        } else {
            retrievalAdapter.nonCacheableSingleModelLoader
        }

    override fun disableCaching() = apply {
        cachingEnabled = false
    }

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

    override fun <QueryClass : Any> queryCustomList(queryModelClass: KClass<QueryClass>,
                                                    databaseWrapper: DatabaseWrapper)
        : MutableList<QueryClass> {
        val query = query
        FlowLog.log(FlowLog.Level.V, "Executing query: $query")
        return getListQueryModelLoader(queryModelClass).load(databaseWrapper, query)!!
    }

    override fun <QueryClass : Any> queryCustomSingle(queryModelClass: KClass<QueryClass>,
                                                      databaseWrapper: DatabaseWrapper)
        : QueryClass? {
        val query = query
        FlowLog.log(FlowLog.Level.V, "Executing query: $query")
        return getSingleQueryModelLoader(queryModelClass).load(databaseWrapper, query)
    }


    protected fun <T : Any> getListQueryModelLoader(table: KClass<T>): ListModelLoader<T> =
        if (cachingEnabled) {
            table.queryModelAdapter.listModelLoader
        } else {
            table.queryModelAdapter.nonCacheableListModelLoader
        }

    protected fun <T : Any> getSingleQueryModelLoader(table: KClass<T>): SingleModelLoader<T> =
        if (cachingEnabled) {
            table.queryModelAdapter.singleModelLoader
        } else {
            table.queryModelAdapter.nonCacheableSingleModelLoader
        }
}
