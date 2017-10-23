package com.raizlabs.android.dbflow.sql.queriable

import com.raizlabs.android.dbflow.structure.database.FlowCursor

/**
 * Description: Responsible for loading data into a single object.
 */
open class SingleModelLoader<TModel>(modelClass: Class<TModel>)
    : ModelLoader<TModel, TModel>(modelClass) {

    open fun convertToData(cursor: FlowCursor, data: TModel?,
                           moveToFirst: Boolean): TModel? {
        var _data = data
        if (!moveToFirst || cursor.moveToFirst()) {
            if (_data == null) {
                _data = instanceAdapter.newInstance()
            }
            instanceAdapter.loadFromCursor(cursor, _data!!)
        }
        return _data
    }

    override fun convertToData(cursor: FlowCursor, data: TModel?): TModel? {
        return convertToData(cursor, data, true)
    }
}
