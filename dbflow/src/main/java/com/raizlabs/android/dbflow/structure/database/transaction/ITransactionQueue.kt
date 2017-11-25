package com.raizlabs.android.dbflow.structure.database.transaction

/**
 * Description: Interface for a queue that manages transactions.
 */
interface ITransactionQueue {

    /**
     * Adds a transaction to the queue.
     *
     * @param transaction The transaction to run on the queue.
     */
    fun add(transaction: Transaction<out Any?>)

    /**
     * Cancels a transaction.
     *
     * @param transaction The transaction to cancel on the queue.
     */
    fun cancel(transaction: Transaction<out Any?>)

    /**
     * Starts if not alive.
     */
    fun startIfNotAlive()

    /**
     * Cancels a transaction by name.
     *
     * @param name the [Transaction.name] property.
     */
    fun cancel(name: String)

    /**
     * Stops/interrupts the queue.
     */
    fun quit()

}
