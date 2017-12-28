package com.raizlabs.android.dbflow.structure.database.transaction;

import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Description: Wraps multiple transactions together.
 */
public class TransactionWrapper implements ITransaction {

    private final List<ITransaction> transactions = new ArrayList<>();

    public TransactionWrapper(ITransaction... transactions) {
        this.transactions.addAll(Arrays.asList(transactions));
    }

    public TransactionWrapper(Collection<ITransaction> transactions) {
        this.transactions.addAll(transactions);
    }

    @Override
    public void execute(DatabaseWrapper databaseWrapper) {
        for (ITransaction transaction : transactions) {
            transaction.execute(databaseWrapper);
        }
    }
}
