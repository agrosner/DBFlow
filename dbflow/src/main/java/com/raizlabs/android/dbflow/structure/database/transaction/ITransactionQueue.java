package com.raizlabs.android.dbflow.structure.database.transaction;

import android.support.annotation.NonNull;

/**
 * Description: Interface for a queue that manages transactions.
 */
public interface ITransactionQueue {

    /**
     * Adds a transaction to the queue.
     *
     * @param transaction The transaction to run on the queue.
     */
    void add(@NonNull Transaction transaction);

    /**
     * Cancels a transaction.
     *
     * @param transaction The transaction to cancel on the queue.
     */
    void cancel(@NonNull Transaction transaction);

    /**
     * Starts if not alive.
     */
    void startIfNotAlive();

    /**
     * Cancels a transaction by name.
     *
     * @param name the {@link Transaction#name()} property.
     */
    void cancel(@NonNull String name);

    /**
     * Stops/interrupts the queue.
     */
    void quit();

}
