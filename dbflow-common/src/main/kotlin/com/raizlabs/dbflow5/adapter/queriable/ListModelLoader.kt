package com.raizlabs.dbflow5.adapter.queriable

import com.raizlabs.dbflow5.KClass
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.database.FlowCursor

/**
 * Description: Loads a [List] of [T].
 */
open class ListModelLoader<T : Any>(modelClass: KClass<T>)
    : ModelLoader<T, MutableList<T>>(modelClass) {

    override fun convertToData(cursor: FlowCursor,
                               databaseWrapper: DatabaseWrapper): MutableList<T> {
        val retData = arrayListOf<T>()
        if (cursor.moveToFirst()) {
            do {
                retData.add(instanceAdapter.loadFromCursor(cursor, databaseWrapper))
            } while (cursor.moveToNext())
        }
        return retData
    }
}
