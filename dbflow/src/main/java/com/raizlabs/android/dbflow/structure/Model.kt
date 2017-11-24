package com.raizlabs.android.dbflow.structure

import com.raizlabs.android.dbflow.config.modelAdapter
import com.raizlabs.android.dbflow.config.writableDatabaseForTable
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper

interface Model : ReadOnlyModel {

    /**
     * Saves the object in the DB.
     *
     * @return true if successful
     */
    fun DatabaseWrapper.save(): Boolean

    /**
     * Deletes the object in the DB
     *
     * @return true if successful
     */
    fun DatabaseWrapper.delete(): Boolean

    /**
     * Updates an object in the DB. Does not insert on failure.
     *
     * @return true if successful
     */
    fun DatabaseWrapper.update(): Boolean

    /**
     * Inserts the object into the DB
     *
     * @return the count of the rows affected, should only be 1 here, or -1 if failed.
     */
    fun DatabaseWrapper.insert(): Long

    companion object {

        /**
         * Returned when [.insert] occurs in an async state or some kind of issue occurs.
         */
        const val INVALID_ROW_ID: Long = -1
    }

}

inline fun <reified T : Any> T.save(databaseWrapper: DatabaseWrapper = writableDatabaseForTable<T>()) = modelAdapter<T>().save(this, databaseWrapper)

inline fun <reified T : Any> T.insert(databaseWrapper: DatabaseWrapper = writableDatabaseForTable<T>()) = modelAdapter<T>().insert(this, databaseWrapper)

inline fun <reified T : Any> T.update(databaseWrapper: DatabaseWrapper = writableDatabaseForTable<T>()) = modelAdapter<T>().update(this, databaseWrapper)

inline fun <reified T : Any> T.delete(databaseWrapper: DatabaseWrapper = writableDatabaseForTable<T>()) = modelAdapter<T>().delete(this, databaseWrapper)

inline fun <reified T : Any> T.exists(databaseWrapper: DatabaseWrapper = writableDatabaseForTable<T>()) = modelAdapter<T>().exists(this, databaseWrapper)
