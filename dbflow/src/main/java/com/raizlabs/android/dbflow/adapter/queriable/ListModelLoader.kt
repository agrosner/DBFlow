package com.raizlabs.android.dbflow.adapter.queriable

import com.raizlabs.android.dbflow.database.DatabaseWrapper
import com.raizlabs.android.dbflow.database.FlowCursor

/**
 * Description: Loads a [List] of [T].
 */
open class ListModelLoader<T : Any>(modelClass: Class<T>)
    : ModelLoader<T, MutableList<T>>(modelClass) {

    override fun load(cursor: FlowCursor?, data: MutableList<T>?,
                      databaseWrapper: DatabaseWrapper): MutableList<T>? {
        var _data = data
        if (_data == null) {
            _data = arrayListOf()
        } else {
            _data.clear()
        }
        return super.load(cursor, _data, databaseWrapper)
    }

    override fun convertToData(cursor: FlowCursor, data: MutableList<T>?,
                               databaseWrapper: DatabaseWrapper): MutableList<T> {
        var _data = data
        if (_data == null) {
            _data = arrayListOf()
        } else {
            _data.clear()
        }

        if (cursor.moveToFirst()) {
            do {
                val model = instanceAdapter.newInstance()
                instanceAdapter.loadFromCursor(cursor, model, databaseWrapper)
                _data.add(model)
            } while (cursor.moveToNext())
        }
        return _data
    }
}
