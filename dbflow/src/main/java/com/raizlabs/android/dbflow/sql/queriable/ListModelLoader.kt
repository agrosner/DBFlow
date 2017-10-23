package com.raizlabs.android.dbflow.sql.queriable

import com.raizlabs.android.dbflow.structure.database.FlowCursor

/**
 * Description: Loads a [List] of [TModel].
 */
open class ListModelLoader<TModel>(modelClass: Class<TModel>)
    : ModelLoader<TModel, MutableList<TModel>>(modelClass) {

    override fun load(cursor: FlowCursor?, data: MutableList<TModel>?): MutableList<TModel>? {
        var _data = data
        if (_data == null) {
            _data = arrayListOf()
        } else {
            _data.clear()
        }
        return super.load(cursor, _data)
    }

    override fun convertToData(cursor: FlowCursor, data: MutableList<TModel>?): MutableList<TModel> {
        var _data = data
        if (_data == null) {
            _data = arrayListOf()
        } else {
            _data.clear()
        }

        if (cursor.moveToFirst()) {
            do {
                val model = instanceAdapter.newInstance()
                instanceAdapter.loadFromCursor(cursor, model)
                _data.add(model)
            } while (cursor.moveToNext())
        }
        return _data
    }
}
