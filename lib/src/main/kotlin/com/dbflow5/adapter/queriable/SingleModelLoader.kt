package com.dbflow5.adapter.queriable

import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.database.FlowCursor
import kotlin.reflect.KClass

/**
 * Description: Responsible for loading data into a single object.
 */
open class SingleModelLoader<T : Any>(modelClass: KClass<T>) : ModelLoader<T, T?>(modelClass) {

    open suspend fun convertToData(
        cursor: FlowCursor,
        moveToFirst: Boolean,
        databaseWrapper: DatabaseWrapper
    ): T? =
        if (!moveToFirst || cursor.moveToFirst()) {
            null //instanceAdapter.loadFromCursor(cursor, databaseWrapper)
        } else null

    override suspend fun convertToData(cursor: FlowCursor, databaseWrapper: DatabaseWrapper): T? {
        return convertToData(cursor, true, databaseWrapper)
    }
}
