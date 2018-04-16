package com.raizlabs.dbflow5.adapter.queriable

import com.raizlabs.dbflow5.KClass
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.database.FlowCursor

/**
 * Description: Responsible for loading data into a single object.
 */
open class SingleModelLoader<T : Any>(modelClass: KClass<T>)
    : ModelLoader<T, T>(modelClass) {

    open fun convertToData(cursor: FlowCursor,
                           moveToFirst: Boolean,
                           databaseWrapper: DatabaseWrapper): T? =
        if (!moveToFirst || cursor.moveToFirst()) {
            instanceAdapter.loadFromCursor(cursor, databaseWrapper)
        } else null

    override fun convertToData(cursor: FlowCursor, databaseWrapper: DatabaseWrapper): T? {
        return convertToData(cursor, true, databaseWrapper)
    }
}
