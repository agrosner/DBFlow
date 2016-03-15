package com.raizlabs.android.dbflow.runtime;

import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;

/**
 * Description: Interface for a queue that manages transactions.
 */
public interface ITransactionQueue {

    /**
     * Adds a transaction to the queue.
     *
     * @param baseTransaction The transaction to run on the queue.
     */
    void add(BaseTransaction baseTransaction);

    /**
     * Cancels a transaction.
     *
     * @param baseTransaction The transaction to cancel on the queue.
     */
    void cancel(BaseTransaction baseTransaction);

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
