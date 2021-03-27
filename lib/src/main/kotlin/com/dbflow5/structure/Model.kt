package com.dbflow5.structure

import com.dbflow5.config.modelAdapter
import com.dbflow5.config.retrievalAdapter
import com.dbflow5.database.DatabaseWrapper

interface Model : ReadOnlyModel {

    /**
     * Saves the object in the DB.
     *
     * @return true if successful
     */
    fun save(wrapper: DatabaseWrapper): Boolean

    /**
     * Deletes the object in the DB
     *
     * @return true if successful
     */
    fun delete(wrapper: DatabaseWrapper): Boolean

    /**
     * Updates an object in the DB. Does not insert on failure.
     *
     * @return true if successful
     */
    fun update(wrapper: DatabaseWrapper): Boolean

    /**
     * Inserts the object into the DB
     *
     * @return the count of the rows affected, should only be 1 here, or -1 if failed.
     */
    fun insert(wrapper: DatabaseWrapper): Long

    companion object {

        /**
         * Returned when [.insert] occurs in an async state or some kind of issue occurs.
         */
        const val INVALID_ROW_ID: Long = -1
    }

}

inline fun <reified T : Any> T.save(databaseWrapper: DatabaseWrapper): Boolean =
        modelAdapter<T>().save(this, databaseWrapper)

inline fun <reified T : Any> T.insert(databaseWrapper: DatabaseWrapper): Long =
        modelAdapter<T>().insert(this, databaseWrapper)

inline fun <reified T : Any> T.update(databaseWrapper: DatabaseWrapper): Boolean =
        modelAdapter<T>().update(this, databaseWrapper)

inline fun <reified T : Any> T.delete(databaseWrapper: DatabaseWrapper): Boolean =
        modelAdapter<T>().delete(this, databaseWrapper)

inline fun <reified T : Any> T.exists(databaseWrapper: DatabaseWrapper): Boolean =
        retrievalAdapter<T>().exists(this, databaseWrapper)

inline fun <reified T : Any> T.load(databaseWrapper: DatabaseWrapper) =
        retrievalAdapter<T>().load(this, databaseWrapper)

