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
        val emptyData = data?.apply { clear() } ?: arrayListOf()
        return super.load(cursor, emptyData, databaseWrapper)
    }

    override fun convertToData(cursor: FlowCursor, data: MutableList<T>?,
                               databaseWrapper: DatabaseWrapper): MutableList<T> {
        val retData = data?.apply { clear() } ?: arrayListOf()
        if (cursor.moveToFirst()) {
            do {
                val model = instanceAdapter.newInstance()
                instanceAdapter.loadFromCursor(cursor, model, databaseWrapper)
                retData.add(model)
            } while (cursor.moveToNext())
        }
        return retData
    }
}
