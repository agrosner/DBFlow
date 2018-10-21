package com.dbflow5.transaction

import com.dbflow5.database.DatabaseWrapper

/**
 * Description: Simplest form of a transaction. It represents an interface by which code is executed
 * inside a database transaction.
 */
interface ITransaction<out R> {

    /**
     * Called within a database transaction.
     *
     * @param databaseWrapper The database to save data into. Use this access to operate on the DB
     * without causing an Android SQLiteDatabaseLockedException or other problems due to locking.
     */
    fun execute(databaseWrapper: DatabaseWrapper): R
}
