package com.raizlabs.android.dbflow.sql.queriable

import com.raizlabs.android.dbflow.structure.database.FlowCursor

/**
 * Description: Responsible for loading data into a single object.
 */
open class SingleModelLoader<T : Any>(modelClass: Class<T>)
    : ModelLoader<T, T>(modelClass) {

    open fun convertToData(cursor: FlowCursor, data: T?,
                           moveToFirst: Boolean): T? {
        var _data = data
        if (!moveToFirst || cursor.moveToFirst()) {
            if (_data == null) {
                _data = instanceAdapter.newInstance()
            }
            instanceAdapter.loadFromCursor(cursor, _data!!)
        }
        return _data
    }

    override fun convertToData(cursor: FlowCursor, data: T?): T? {
        return convertToData(cursor, data, true)
    }
}
