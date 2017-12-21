package com.raizlabs.dbflow5.adapter.queriable

import com.raizlabs.dbflow5.adapter.RetrievalAdapter
import com.raizlabs.dbflow5.config.FlowManager
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.database.FlowCursor

/**
 * Description: Represents how models load from DB. It will query a [DatabaseWrapper]
 * and query for a [FlowCursor]. Then the cursor is used to convert itself into an object.
 */
abstract class ModelLoader<TModel : Any, out TReturn : Any>(val modelClass: Class<TModel>) {

    val instanceAdapter: RetrievalAdapter<TModel> by lazy { FlowManager.getRetrievalAdapter(modelClass) }

    /**
     * Loads the data from a query and returns it as a [TReturn].
     *
     * @param databaseWrapper A custom database wrapper object to use.
     * @param query           The query to call.
     * @return The data loaded from the database.
     */
    open fun load(databaseWrapper: DatabaseWrapper, query: String): TReturn?
        = load(databaseWrapper.rawQuery(query, null), databaseWrapper)

    open fun load(cursor: FlowCursor?, databaseWrapper: DatabaseWrapper): TReturn? {
        var data: TReturn? = null
        cursor?.use { data = convertToData(it, databaseWrapper) }
        return data
    }

    /**
     * Specify how to convert the [FlowCursor] data into a [TReturn]. Can be null.
     *
     * @param cursor The cursor resulting from a query passed into [.load]
     * @return A new (or reused) instance that represents the [FlowCursor].
     */
    abstract fun convertToData(cursor: FlowCursor, databaseWrapper: DatabaseWrapper): TReturn?
}
