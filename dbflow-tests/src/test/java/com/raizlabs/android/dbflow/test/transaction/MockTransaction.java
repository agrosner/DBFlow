package com.raizlabs.android.dbflow.test.transaction;

import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

/**
 * Description: Provides a way to mock transactions.
 */
public class MockTransaction {

    private final Transaction transaction;
    private final DatabaseWrapper databaseWrapper;

    public MockTransaction(Transaction transaction, DatabaseWrapper databaseWrapper) {
        this.transaction = transaction;
        this.databaseWrapper = databaseWrapper;
    }

    public void execute() {
        transaction.transaction().execute(databaseWrapper);
    }
}
