package com.raizlabs.android.dbflow.sql.queriable

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

import com.raizlabs.android.dbflow.config.DatabaseDefinition
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.structure.InstanceAdapter
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper
import com.raizlabs.android.dbflow.structure.database.FlowCursor

/**
 * Description: Represents how models load from DB. It will query a [SQLiteDatabase]
 * and query for a [Cursor]. Then the cursor is used to convert itself into an object.
 */
abstract class ModelLoader<TModel : Any, TReturn : Any>(val modelClass: Class<TModel>) {

    val databaseDefinition: DatabaseDefinition by lazy { FlowManager.getDatabaseForTable(modelClass) }
    val instanceAdapter: InstanceAdapter<TModel> by lazy { FlowManager.getInstanceAdapter(modelClass) }

    /**
     * Loads the data from a query and returns it as a [TReturn].
     *
     * @param query The query to call.
     * @return The data loaded from the database.
     */
    open fun load(query: String): TReturn? {
        return load(databaseDefinition.writableDatabase, query)
    }

    open fun load(query: String, data: TReturn?): TReturn? {
        return load(databaseDefinition.writableDatabase, query, data)
    }

    /**
     * Loads the data from a query and returns it as a [TReturn].
     *
     * @param databaseWrapper A custom database wrapper object to use.
     * @param query           The query to call.
     * @return The data loaded from the database.
     */
    open fun load(databaseWrapper: DatabaseWrapper, query: String): TReturn? {
        return load(databaseWrapper, query, null)
    }

    open fun load(databaseWrapper: DatabaseWrapper, query: String,
                  data: TReturn?): TReturn? {
        return load(databaseWrapper.rawQuery(query, null), data)
    }

    open fun load(cursor: FlowCursor?): TReturn? = load(cursor, null)

    open fun load(cursor: FlowCursor?, data: TReturn?): TReturn? {
        var _data = data
        cursor?.use { _data = convertToData(it, _data) }
        return _data
    }

    /**
     * Specify how to convert the [Cursor] data into a [TReturn]. Can be null.
     *
     * @param cursor The cursor resulting from a query passed into [.load]
     * @param data   The data (if not null) that we can reuse without need to create new object.
     * @return A new (or reused) instance that represents the [Cursor].
     */
    abstract fun convertToData(cursor: FlowCursor, data: TReturn?): TReturn?
}
