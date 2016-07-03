package com.raizlabs.android.dbflow.test.transaction;

import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

/**
 * Description: Provides a way to mock transactions.
 */
public class MockTransaction {

    private final Transaction transaction;
    private final DatabaseDefinition databaseDefinition;

    public MockTransaction(Transaction transaction, DatabaseDefinition databaseDefinition) {
        this.transaction = transaction;
        this.databaseDefinition = databaseDefinition;
    }

    public void execute() {
        transaction.transaction().execute(databaseDefinition.getWritableDatabase());
    }
}
