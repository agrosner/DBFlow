package com.dbflow5.structure

import com.dbflow5.config.modelAdapter
import com.dbflow5.config.retrievalAdapter
import com.dbflow5.database.DatabaseWrapper

object Model {

    /**
     * Returned when [.insert] occurs in an async state or some kind of issue occurs.
     */
    const val INVALID_ROW_ID: Long = -1
}

suspend inline fun <reified T : Any> T.save(databaseWrapper: DatabaseWrapper) =
    modelAdapter<T>().save(this, databaseWrapper)

suspend inline fun <reified T : Any> T.insert(databaseWrapper: DatabaseWrapper) =
    modelAdapter<T>().insert(this, databaseWrapper)

suspend inline fun <reified T : Any> T.update(databaseWrapper: DatabaseWrapper) =
    modelAdapter<T>().update(this, databaseWrapper)

suspend inline fun <reified T : Any> T.delete(databaseWrapper: DatabaseWrapper) =
    modelAdapter<T>().delete(this, databaseWrapper)

suspend inline fun <reified T : Any> T.exists(databaseWrapper: DatabaseWrapper) =
    retrievalAdapter<T>().exists(this, databaseWrapper)

suspend inline fun <reified T : Any> T.load(databaseWrapper: DatabaseWrapper) =
    retrievalAdapter<T>().load(this, databaseWrapper)

