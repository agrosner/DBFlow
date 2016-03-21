package com.raizlabs.android.dbflow.runtime;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.structure.database.transaction.ITransactionQueue;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

/**
 * Description: The base implementation of Transaction manager.
 */
public abstract class BaseTransactionManager {

    private final ITransactionQueue transactionQueue;
    private final DatabaseDefinition databaseDefinition;
    private DBBatchSaveQueue saveQueue;

    public BaseTransactionManager(@NonNull ITransactionQueue transactionQueue, @NonNull DatabaseDefinition databaseDefinition) {
        this.transactionQueue = transactionQueue;
        this.databaseDefinition = databaseDefinition;
        saveQueue = new DBBatchSaveQueue(databaseDefinition);
        checkQueue();
    }

    public DBBatchSaveQueue getSaveQueue() {
        try {
            if (!saveQueue.isAlive()) {
                saveQueue.start();
            }
        } catch (IllegalThreadStateException i) {
            FlowLog.logError(i); // if queue is alive, will throw error. might occur in multithreading.
        }
        return saveQueue;
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
