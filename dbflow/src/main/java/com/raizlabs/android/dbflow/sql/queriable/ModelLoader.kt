package com.raizlabs.android.dbflow.sql.queriable

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.structure.InstanceAdapter
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper
import com.raizlabs.android.dbflow.structure.database.FlowCursor

/**
 * Description: Represents how models load from DB. It will query a [SQLiteDatabase]
 * and query for a [Cursor]. Then the cursor is used to convert itself into an object.
 */
abstract class ModelLoader<TModel : Any, TReturn : Any>(val modelClass: Class<TModel>) {

    val instanceAdapter: InstanceAdapter<TModel> by lazy { FlowManager.getInstanceAdapter(modelClass) }

    /**
     * Loads the data from a query and returns it as a [TReturn].
     *
     * @param databaseWrapper A custom database wrapper object to use.
     * @param query           The query to call.
     * @return The data loaded from the database.
     */
    open fun load(databaseWrapper: DatabaseWrapper, query: String): TReturn?
            = load(databaseWrapper, query, null)

    open fun load(databaseWrapper: DatabaseWrapper, query: String, data: TReturn?): TReturn?
            = load(databaseWrapper.rawQuery(query, null), data, databaseWrapper)

    open fun load(cursor: FlowCursor?, databaseWrapper: DatabaseWrapper): TReturn? = load(cursor, null, databaseWrapper)

    open fun load(cursor: FlowCursor?, data: TReturn?, databaseWrapper: DatabaseWrapper): TReturn? {
        var _data = data
        cursor?.use { _data = convertToData(it, _data, databaseWrapper) }
        return _data
    }

    /**
     * Specify how to convert the [Cursor] data into a [TReturn]. Can be null.
     *
     * @param cursor The cursor resulting from a query passed into [.load]
     * @param data   The data (if not null) that we can reuse without need to create new object.
     * @return A new (or reused) instance that represents the [Cursor].
     */
    abstract fun convertToData(cursor: FlowCursor, data: TReturn?, databaseWrapper: DatabaseWrapper): TReturn?
}
