package com.raizlabs.android.dbflow.transaction

import android.database.sqlite.SQLiteDatabaseLockedException

import com.raizlabs.android.dbflow.database.DatabaseWrapper

/**
 * Description: Simplest form of a transaction. It represents an interface by which code is executed
 * inside a database transaction.
 */
interface ITransaction<out R> {

    /**
     * Called within a database transaction.
     *
     * @param databaseWrapper The database to save data into. Use this access to operate on the DB
     * without causing [SQLiteDatabaseLockedException].
     */
    fun execute(databaseWrapper: DatabaseWrapper): R
}
