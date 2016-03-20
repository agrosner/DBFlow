package com.raizlabs.android.dbflow.structure.database.transaction;

import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: Simplest form of a transaction. It represents an interface by which code is executed
 * inside a database transaction.
 */
public interface ITransaction {

    /**
     * Called within a database transaction.
     *
     * @param databaseWrapper The database to save data into.
     */
    void execute(DatabaseWrapper databaseWrapper);
}
