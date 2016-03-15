package com.raizlabs.android.dbflow.runtime;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: The base implementation of Transaction manager.
 */
public abstract class BaseTransactionManager {

    private final ITransactionQueue transactionQueue;

    public BaseTransactionManager(@NonNull ITransactionQueue transactionQueue) {
        this.transactionQueue = transactionQueue;
        checkQueue();
    }

    /**
     * Wraps the runnable around {@link android.database.sqlite.SQLiteDatabase#beginTransaction()} and the other methods.
     *
     * @param runnable The runnable to transact.
     */
    public static void transact(String databaseName, Runnable runnable) {
        transact(FlowManager.getDatabase(databaseName).getWritableDatabase(), runnable);
    }

    /**
     * Wraps the runnable around {@link android.database.sqlite.SQLiteDatabase#beginTransaction()} and the other methods.
     *
     * @param runnable The runnable to transact.
     */
    public static void transact(DatabaseWrapper database, Runnable runnable) {
        database.beginTransaction();
        try {
            runnable.run();
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
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
    public void addTransaction(BaseTransaction transaction) {
        getQueue().add(transaction);
    }

    /**
     * Cancels a transaction on the {@link ITransactionQueue}.
     *
     * @param transaction
     */
    public void cancelTransaction(BaseTransaction transaction) {
        getQueue().cancel(transaction);
    }
}
