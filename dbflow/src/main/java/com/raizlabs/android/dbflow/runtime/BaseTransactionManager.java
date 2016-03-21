package com.raizlabs.android.dbflow.runtime;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.structure.database.transaction.ITransactionQueue;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

/**
 * Description: The base implementation of Transaction manager.
 */
public abstract class BaseTransactionManager {

    private final ITransactionQueue transactionQueue;

    public BaseTransactionManager(@NonNull ITransactionQueue transactionQueue) {
        this.transactionQueue = transactionQueue;
        checkQueue();
    }

    public ITransactionQueue getQueue() {
        return transactionQueue;
    }

    /**
     * Checks if queue is running. If not, should be started here.
     */
    public void checkQueue() {
        getQueue().startIfNotAlive();
    }

    /**
     * Stops the queue this manager contains.
     */
    public void stopQueue() {
        getQueue().quit();
    }

    /**
     * Adds a transaction to the {@link ITransactionQueue}.
     *
     * @param transaction The transaction to add.
     */
    public void addTransaction(Transaction transaction) {
        getQueue().add(transaction);
    }

    /**
     * Cancels a transaction on the {@link ITransactionQueue}.
     *
     * @param transaction
     */
    public void cancelTransaction(Transaction transaction) {
        getQueue().cancel(transaction);
    }
}
