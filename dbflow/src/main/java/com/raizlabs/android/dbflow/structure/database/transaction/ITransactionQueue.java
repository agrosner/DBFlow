package com.raizlabs.android.dbflow.structure.database.transaction;

import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;

/**
 * Description: Interface for a queue that manages transactions.
 */
public interface ITransactionQueue {

    /**
     * Adds a transaction to the queue.
     *
     * @param transaction The transaction to run on the queue.
     */
    void add(Transaction transaction);

    /**
     * Cancels a transaction.
     *
     * @param transaction The transaction to cancel on the queue.
     */
    void cancel(Transaction transaction);

    /**
     * Starts if not alive.
     */
    void startIfNotAlive();

    /**
     * Cancels a transaction by name.
     *
     * @param name the {@link BaseTransaction#getName()} property.
     */
    void cancel(String name);

    /**
     * Stops/interrupts the queue.
     */
    void quit();

}
