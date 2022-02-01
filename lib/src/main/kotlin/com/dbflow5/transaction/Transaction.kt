package com.dbflow5.transaction

import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.config.FlowLog
import com.dbflow5.database.DatabaseWrapper
import kotlinx.coroutines.Job

typealias Ready<R> = (Transaction<R>) -> Unit
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
data class Transaction<R : Any?>(
    @get:JvmName("transaction")
    val transaction: SuspendableTransaction<R>,
    private val databaseDefinition: DBFlowDatabase,
    @get:JvmName("ready")
    val ready: Ready<R>? = null,
    @get:JvmName("error")
    val error: Error<R>? = null,
    @get:JvmName("success")
    val success: Success<R>? = null,
    @get:JvmName("completion")
    val completion: Completion<R>? = null,
    @get:JvmName("name")
    val name: String?,
    private val shouldRunInTransaction: Boolean = true,
    private val runCallbacksOnSameThread: Boolean = true
) : SuspendableTransaction<Result<R>> {

    /**
     * Used with [enqueue], when active will enable cancellation.
     */
    private var activeJob: Job? = null

    internal constructor(builder: Builder<R>) : this(
        databaseDefinition = builder.database,
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
     *
     * Suspends until its completion.
     */
    suspend fun execute() = apply {
        databaseDefinition.executeTransaction(this)
    }

    /**
     * Enqueues the transaction result, and returns the associated Job.
     */
    fun enqueue() = apply {
        this.activeJob = databaseDefinition.enqueueTransaction(this)
    }

    /**
     * Cancels a transaction that has not run yet.
     */
    fun cancel() {
        this.activeJob?.cancel()
    }

    /**
     * Executes the transaction immediately on the same thread from which it is called. This calls
     * the [DBFlowDatabase.executeTransactionForResult] method, which runs the
     * [.transaction] in a database transaction.
     */
    override suspend fun execute(db: DatabaseWrapper): Result<R> = try {
        ready?.invoke(this)

        val result: R = if (shouldRunInTransaction) {
            databaseDefinition.executeTransactionForResult { transaction.execute(it) }
        } else {
            transaction.execute(databaseDefinition)
        }
        if (success != null) {
            success.invoke(this, result)
            complete()
        }
        Result.success(result)
    } catch (throwable: Throwable) {
        FlowLog.logError(throwable)
        if (error != null) {
            error.invoke(this, throwable)
            complete()
            Result.failure(throwable)
        } else {
            Result.failure(
                RuntimeException(
                    "An exception occurred while executing a transaction",
                    throwable
                )
            )
        }
    }

    private fun complete() = completion?.invoke(this)

    fun newBuilder(): Builder<R> {
        return Builder(transaction, databaseDefinition)
            .error(error)
            .success(success)
            .name(name)
            .shouldRunInTransaction(shouldRunInTransaction)
    }

    /**
     * The main entry point into [Transaction], this provides an easy way to build up transactions.
     */
    class Builder<R : Any?>
    /**
     * @param transaction        The interface that actually executes the transaction.
     * @param database The database this transaction will run on. Should be the same
     * DB as the code that the transaction runs in.
     */
        (
        internal val transaction: SuspendableTransaction<R>,
        internal val database: DBFlowDatabase
    ) {
        internal var ready: Ready<R>? = null
        internal var errorCallback: Error<R>? = null
        internal var successCallback: Success<R>? = null
        internal var completion: Completion<R>? = null
        internal var name: String? = null
        internal var shouldRunInTransaction = true
        internal var runCallbacksOnSameThread: Boolean = false

        /**
         * Specify a callback when the transaction is ready to execute. Do an initialization here,
         * and cleanup on [completion]
         */
        fun ready(ready: Ready<R>?) = apply {
            this.ready = ready
        }

        /**
         * Specify an error callback to return all and any [Throwable] that occurred during a [Transaction].
         * @param error Invoked on the UI thread, unless [runCallbacksOnSameThread] is true.
         */
        fun error(error: Error<R>?) = apply {
            this.errorCallback = error
        }

        /**
         * Specify a listener for successful transactions. This is called when the [SuspendableTransaction]
         * specified is finished and it is posted on the UI thread.
         *
         * @param success The callback, invoked on the UI thread, unless [runCallbacksOnSameThread] is true.
         */
        fun success(success: Success<R>?) = apply {
            this.successCallback = success
        }

        /**
         * Runs exactly once, no matter if it was successful or failed, at the end of the execution
         * of this transaction.
         * @param completion Invoked on the UI thread, unless [runCallbacksOnSameThread] is true.
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
         * @return A new instance of [Transaction]. Subsequent calls to this method produce
         * new instances.
         */
        fun build(): Transaction<R> = Transaction(this)

        /**
         * Convenience method to simply execute a transaction.
         */
        @JvmOverloads
        fun enqueue(
            ready: Ready<R>? = null,
            error: Error<R>? = null,
            completion: Completion<R>? = null,
            success: Success<R>? = null
        ) =
            this.apply {
                ready?.let(this::ready)
                success?.let(this::success)
                error?.let(this::error)
                completion?.let(this::completion)
            }.build().enqueue()

        /**
         * Convenience method to simply execute a transaction.
         */
        @JvmOverloads
        suspend fun execute(
            ready: Ready<R>? = null,
            error: Error<R>? = null,
            completion: Completion<R>? = null,
            success: Success<R>? = null
        ) =
            this.apply {
                ready?.let(this::ready)
                success?.let(this::success)
                error?.let(this::error)
                completion?.let(this::completion)
            }.build().execute()
    }

}

