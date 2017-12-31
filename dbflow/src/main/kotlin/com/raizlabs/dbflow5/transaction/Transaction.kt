package com.raizlabs.dbflow5.transaction

import android.os.Handler
import android.os.Looper

import com.raizlabs.dbflow5.config.DBFlowDatabase
import com.raizlabs.dbflow5.config.FlowLog


typealias Success<R> = (Transaction<R>, R) -> Unit
typealias Error<R> = (Transaction<R>, Throwable) -> Unit
typealias Completion<R> = (Transaction<R>) -> Unit

/**
 * Description: The main transaction class. It represents a transaction that occurs in the database.
 * This is a handy class that allows you to wrap up a set of database modification (or queries) into
 * a code block that gets accessed all on the same thread, in the same queue. This can prevent locking
 * and synchronization issues when trying to read and write from the database at the same time.
 *
 *
 * To create one, the recommended method is to use the [DBFlowDatabase.beginTransactionAsync].
 */
class Transaction<R : Any?>(
    @get:JvmName("transaction")
    val transaction: ITransaction<R>,
    private val databaseDefinition: DBFlowDatabase,
    @get:JvmName("error")
    val error: Error<R>? = null,
    @get:JvmName("success")
    val success: Success<R>? = null,
    @get:JvmName("completion")
    val completion: Completion<R>? = null,
    @get:JvmName("name")
    val name: String?,
    private val shouldRunInTransaction: Boolean = true,
    private val runCallbacksOnSameThread: Boolean = true) {


    internal constructor(builder: Builder<R>) : this(
        databaseDefinition = builder.databaseDefinition,
        error = builder.errorCallback,
        success = builder.successCallback,
        completion = builder.completion,
        transaction = builder.transaction,
        name = builder.name,
        shouldRunInTransaction = builder.shouldRunInTransaction,
        runCallbacksOnSameThread = builder.runCallbacksOnSameThread
    )

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
     * the [DBFlowDatabase.executeTransaction] method, which runs the
     * [.transaction] in a database transaction.
     */
    fun executeSync() {
        try {
            val result = if (shouldRunInTransaction) {
                databaseDefinition.executeTransaction(transaction)
            } else {
                transaction.execute(databaseDefinition)
            }
            if (success != null) {
                if (runCallbacksOnSameThread) {
                    success.invoke(this, result)
                    complete()
                } else {
                    transactionHandler.post {
                        success.invoke(this@Transaction, result)
                        complete()
                    }
                }
            }
        } catch (throwable: Throwable) {
            FlowLog.logError(throwable)
            if (error != null) {
                if (runCallbacksOnSameThread) {
                    error.invoke(this, throwable)
                    complete()
                } else {
                    transactionHandler.post {
                        error.invoke(this@Transaction, throwable)
                        complete()
                    }
                }
            } else {
                throw RuntimeException("An exception occurred while executing a transaction", throwable)
            }
        }
    }

    private fun complete() = completion?.invoke(this)

    fun newBuilder(): Builder<R> {
        return Builder(transaction, databaseDefinition)
            .error(error)
            .success(success)
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
    (internal val transaction: ITransaction<R>, internal val databaseDefinition: DBFlowDatabase) {
        internal var errorCallback: Error<R>? = null
        internal var successCallback: Success<R>? = null
        internal var completion: Completion<R>? = null
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
         * Specify a listener for successful transactions. This is called when the [ITransaction]
         * specified is finished and it is posted on the UI thread.
         *
         * @param successCallback The callback, invoked on the UI thread.
         */
        fun success(successCallback: Success<R>?) = apply {
            this.successCallback = successCallback
        }

        /**
         * Runs exactly once, no matter if it was successful or failed, at the end of the execution
         * of this transaction.
         */
        fun completion(completion: Completion<R>?) = apply {
            this.completion = completion
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

