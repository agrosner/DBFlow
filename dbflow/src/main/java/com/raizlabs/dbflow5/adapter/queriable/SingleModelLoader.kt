package com.raizlabs.dbflow5.adapter.queriable

import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.database.FlowCursor

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
