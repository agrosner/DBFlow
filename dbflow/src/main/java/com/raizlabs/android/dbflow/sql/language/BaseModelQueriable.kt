package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.config.FlowLog
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.list.FlowCursorList
import com.raizlabs.android.dbflow.list.FlowQueryList
import com.raizlabs.android.dbflow.runtime.NotifyDistributor
import com.raizlabs.android.dbflow.sql.Query
import com.raizlabs.android.dbflow.sql.queriable.AsyncQuery
import com.raizlabs.android.dbflow.sql.queriable.ListModelLoader
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable
import com.raizlabs.android.dbflow.sql.queriable.SingleModelLoader
import com.raizlabs.android.dbflow.structure.InstanceAdapter
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper

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
protected constructor(val databaseWrapper: DatabaseWrapper,
                      table: Class<TModel>)
    : BaseQueriable<TModel>(databaseWrapper, table), ModelQueriable<TModel>, Query {

    private val retrievalAdapter: InstanceAdapter<TModel> by lazy { FlowManager.getInstanceAdapter(table) }

    private val listModelLoader: ListModelLoader<TModel>
        get() = retrievalAdapter.listModelLoader

    private val singleModelLoader: SingleModelLoader<TModel>
        get() = retrievalAdapter.singleModelLoader

    override fun queryResults(): CursorResult<TModel> = CursorResult(retrievalAdapter.modelClass, query(), databaseWrapper)

    override fun queryList(): MutableList<TModel> {
        val query = query
        FlowLog.log(FlowLog.Level.V, "Executing query: " + query)
        return listModelLoader.load(databaseWrapper, query)!!
    }

    override fun querySingle(): TModel? {
        val query = query
        FlowLog.log(FlowLog.Level.V, "Executing query: " + query)
        return singleModelLoader.load(databaseWrapper, query)
    }

    override fun cursorList(): FlowCursorList<TModel> = FlowCursorList.Builder(this).build()

    override fun flowQueryList(): FlowQueryList<TModel> =
            FlowQueryList.Builder(modelQueriable = this).build()

    override fun executeUpdateDelete(): Long {
        val affected = databaseWrapper.compileStatement(query).executeUpdateDelete()

        // only notify for affected.
        if (affected > 0) {
            NotifyDistributor.get().notifyTableChanged(table, primaryAction)
        }
        return affected
    }

    override fun async(): AsyncQuery<TModel> = AsyncQuery(this)

    override fun <QueryClass : Any> queryCustomList(queryModelClass: Class<QueryClass>): MutableList<QueryClass> {
        val query = query
        FlowLog.log(FlowLog.Level.V, "Executing query: " + query)
        val adapter = FlowManager.getQueryModelAdapter(queryModelClass)
        return adapter.listModelLoader.load(databaseWrapper, query)!!
    }

    override fun <QueryClass : Any> queryCustomSingle(queryModelClass: Class<QueryClass>): QueryClass? {
        val query = query
        FlowLog.log(FlowLog.Level.V, "Executing query: " + query)
        val adapter = FlowManager.getQueryModelAdapter(queryModelClass)
        return adapter.singleModelLoader.load(databaseWrapper, query)
    }

}
