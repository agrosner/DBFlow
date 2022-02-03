package com.dbflow5.structure

import com.dbflow5.config.modelAdapter
import com.dbflow5.database.DatabaseWrapper

object Model {

    /**
     * Returned when [.insert] occurs in an async state or some kind of issue occurs.
     */
    const val INVALID_ROW_ID: Long = -1
}

inline fun <reified T : Any> T.insert(databaseWrapper: DatabaseWrapper) =
    modelAdapter<T>().insert(this, databaseWrapper)

