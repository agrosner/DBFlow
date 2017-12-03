package com.raizlabs.dbflow5.transaction

import android.os.Handler
import android.os.Looper

import com.raizlabs.dbflow5.config.DatabaseDefinition
import com.raizlabs.dbflow5.config.FlowLog

/**
 * Description: The main transaction class. It represents a transaction that occurs in the database.
 * This is a handy class that allows you to wrap up a set of database modification (or queries) into
 * a code block that gets accessed all on the same thread, in the same queue. This can prevent locking
 * and synchronization issues when trying to read and write from the database at the same time.
 *
 *
 * To create one, the recommended method is to use the [DatabaseDefinition.beginTransactionAsync].
 */
class Transaction<R : Any?>(private val transaction: ITransaction<R>,
                            private val databaseDefinition: DatabaseDefinition,
                            private val errorCallback: Error<R>? = null,
                            private val successCallback: Success<R>? = null,
                            private val name: String?,
                            private val shouldRunInTransaction: Boolean = true,
                            private val runCallbacksOnSameThread: Boolean = true) {


    /**
     * Callback when a [ITransaction] failed because of an exception.
     */
    interface Error<R : Any?> {

        /**
         * Called when transaction fails.
         *
         * @param transaction The transaction that failed.
         * @param error       The error that was thrown.
         */
        fun onError(transaction: Transaction<R>, error: Throwable)
    }

    /**
     * Interface callback when a [ITransaction] was successful.
     */
    interface Success<R : Any?> {

        /**
         * Called when a transaction succeeded.
         *
         * @param transaction The transaction that succeeded.
         */
        fun onSuccess(transaction: Transaction<R>, result: R)
    }


    internal constructor(builder: Builder<R>) : this(
            databaseDefinition = builder.databaseDefinition,
            errorCallback = builder.errorCallback,
            successCallback = builder.successCallback,
            transaction = builder.transaction,
            name = builder.name,
            shouldRunInTransaction = builder.shouldRunInTransaction,
            runCallbacksOnSameThread = builder.runCallbacksOnSameThread
    )

    fun error(): Error<R>? = errorCallback

    fun success(): Success<R>? = successCallback

    fun transaction(): ITransaction<R> = transaction

    fun name(): String? = name

    /**
     * Runs the transaction in the [BaseTransactionManager] of the associated database.
     */
    fun execute() = apply {
        databaseDefinition.transactionManager.addTransaction(this)
    }

    /**
     * Cancels a transaction that has not run yet.
     */
    fun cancel() {
        databaseDefinition.transactionManager.cancelTransaction(this)
    }

    /**
     * Executes the transaction immediately on the same thread from which it is called. This calls
     * the [DatabaseDefinition.executeTransaction] method, which runs the
     * [.transaction] in a database transaction.
     */
    fun executeSync() {
        try {
            val result = if (shouldRunInTransaction) {
                databaseDefinition.executeTransaction(transaction)
            } else {
                transaction.execute(databaseDefinition.writableDatabase)
            }
            if (successCallback != null) {
                if (runCallbacksOnSameThread) {
                    successCallback.onSuccess(this, result)
                } else {
                    transactionHandler.post { successCallback.onSuccess(this@Transaction, result) }
                }
            }
        } catch (throwable: Throwable) {
            FlowLog.logError(throwable)
            if (errorCallback != null) {
                if (runCallbacksOnSameThread) {
                    errorCallback.onError(this, throwable)
                } else {
                    transactionHandler.post { errorCallback.onError(this@Transaction, throwable) }
                }
            } else {
                throw RuntimeException("An exception occurred while executing a transaction", throwable)
            }
        }

    }

    fun newBuilder(): Builder<R> {
        return Builder(transaction, databaseDefinition)
                .error(errorCallback)
                .success(successCallback)
                .name(name)
                .shouldRunInTransaction(shouldRunInTransaction)
                .runCallbacksOnSameThread(runCallbacksOnSameThread)
    }

    /**
     * The main entry point into [Transaction], this provides an easy way to build up transactions.
     */
    class Builder<R : Any?>
    /**
     * @param transaction        The interface that actually executes the transaction.
     * @param databaseDefinition The database this transaction will run on. Should be the same
     * DB as the code that the transaction runs in.
     */
    (internal val transaction: ITransaction<R>, internal val databaseDefinition: DatabaseDefinition) {
        internal var errorCallback: Error<R>? = null
        internal var successCallback: Success<R>? = null
        internal var name: String? = null
        internal var shouldRunInTransaction = true
        internal var runCallbacksOnSameThread: Boolean = false

        /**
         * Specify an error callback to return all and any [Throwable] that occured during a [Transaction].
         */
        fun error(errorCallback: Error<R>?) = apply {
            this.errorCallback = errorCallback
        }

        /**
         * Specify an error callback to return all and any [Throwable] that occured during a [Transaction].
         */
        fun error(errorCallback: ((Transaction<R>, Throwable) -> Unit)?) = apply {
            if (errorCallback != null) {
                this.errorCallback = transactionError { transaction, throwable -> errorCallback(transaction, throwable) }
            }
        }

        /**
         * Specify a listener for successful transactions. This is called when the [ITransaction]
         * specified is finished and it is posted on the UI thread.
         *
         * @param successCallback The callback, invoked on the UI thread.
         */
        fun success(successCallback: Success<R>?) = apply {
            this.successCallback = successCallback
        }

        /**
         * Specify a listener for successful transactions. This is called when the [ITransaction]
         * specified is finished and it is posted on the UI thread.
         *
         * @param successCallback The callback, invoked on the UI thread.
         */
        fun success(successCallback: ((Transaction<R>, R) -> Unit)?) = apply {
            if (successCallback != null) {
                this.successCallback = transactionSuccess { transaction, r -> successCallback(transaction, r) }
            }
        }

        /**
         * Give this transaction a name. This will allow you to call [ITransactionQueue.cancel].
         *
         * @param name The name of this transaction. Should be unique for any transaction currently
         * running in the [ITransactionQueue].
         */
        fun name(name: String?) = apply {
            this.name = name
        }

        /**
         * @param shouldRunInTransaction True is default. If true, we run this [Transaction] in
         * a database transaction. If this is not necessary (usually for
         * [QueryTransaction]), you should specify false.
         * @return
         */
        fun shouldRunInTransaction(shouldRunInTransaction: Boolean) = apply {
            this.shouldRunInTransaction = shouldRunInTransaction
        }

        /**
         * @param runCallbacksOnSameThread Default is false. If true we return the callbacks from
         * this [Transaction] on the same thread we call
         * [.execute] from.
         */
        fun runCallbacksOnSameThread(runCallbacksOnSameThread: Boolean) = apply {
            this.runCallbacksOnSameThread = runCallbacksOnSameThread
        }

        /**
         * @return A new instance of [Transaction]. Subsequent calls to this method produce
         * new instances.
         */
        fun build(): Transaction<R> = Transaction(this)

        /**
         * Convenience method to simply execute a transaction.
         */
        fun execute() = build().execute()
    }

    companion object {

        internal val transactionHandler: Handler by lazy { Handler(Looper.getMainLooper()) }
    }
}

inline fun <R : Any?> transactionSuccess(crossinline function: (Transaction<R>, R) -> Unit) = object : Transaction.Success<R> {
    override fun onSuccess(transaction: Transaction<R>, result: R) = function(transaction, result)
}

inline fun <R : Any?> transactionError(crossinline function: (Transaction<R>, Throwable) -> Unit) = object : Transaction.Error<R> {
    override fun onError(transaction: Transaction<R>, error: Throwable) = function(transaction, error)
}
