package com.raizlabs.android.dbflow.adapter.queriable

import com.raizlabs.android.dbflow.database.DatabaseWrapper
import com.raizlabs.android.dbflow.database.FlowCursor

/**
 * Description: Responsible for loading data into a single object.
 */
open class SingleModelLoader<T : Any>(modelClass: Class<T>)
    : ModelLoader<T, T>(modelClass) {

    open fun convertToData(cursor: FlowCursor, data: T?,
                           moveToFirst: Boolean,
                           databaseWrapper: DatabaseWrapper): T? {
        var _data = data
        if (!moveToFirst || cursor.moveToFirst()) {
            if (_data == null) {
                _data = instanceAdapter.newInstance()
            }
            instanceAdapter.loadFromCursor(cursor, _data, databaseWrapper)
        }
        return _data
    }

    override fun convertToData(cursor: FlowCursor, data: T?, databaseWrapper: DatabaseWrapper): T? {
        return convertToData(cursor, data, true, databaseWrapper)
    }
}
