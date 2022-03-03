package com.dbflow5.transaction

import com.dbflow5.config.FlowLog
import com.dbflow5.config.GeneratedDatabase
import com.dbflow5.config.enqueueTransaction
import com.dbflow5.config.executeTransactionOnDispatcher
import com.dbflow5.database.scope.WritableDatabaseScope
import kotlinx.coroutines.Job
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads

typealias Ready<DB, R> = (Transaction<DB, R>) -> Unit
typealias Success<DB, R> = (Transaction<DB, R>, R) -> Unit
typealias Error<DB, R> = (Transaction<DB, R>, Throwable) -> Unit
typealias Completion<DB, R> = (Transaction<DB, R>) -> Unit

/**
 * Description: The main transaction class. It represents a transaction that occurs in the database.
 * This is a handy class that allows you to wrap up a set of database modification (or queries) into
 * a code block that gets accessed all on the same thread, in the same queue. This can prevent locking
 * and synchronization issues when trying to read and write from the database at the same time.
 *
 *
 * To create one, the recommended method is to use the [DBFlowDatabase.beginTransactionAsync].
 */
data class Transaction<DB : GeneratedDatabase, R : Any?>(
    @get:JvmName("transaction")
    val transaction: SuspendableTransaction<DB, R>,
    private val databaseDefinition: DB,
    @get:JvmName("ready")
    val ready: Ready<DB, R>? = null,
    @get:JvmName("error")
    val error: Error<DB, R>? = null,
    @get:JvmName("success")
    val success: Success<DB, R>? = null,
    @get:JvmName("completion")
    val completion: Completion<DB, R>? = null,
    @get:JvmName("name")
    val name: String?,
    private val shouldRunInTransaction: Boolean = true,
    private val runCallbacksOnSameThread: Boolean = true
) : SuspendableTransaction<DB, Result<R>> {

    /**
     * Used with [enqueue], when active will enable cancellation.
     */
    private var activeJob: Job? = null

    internal constructor(builder: Builder<DB, R>) : this(
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
        databaseDefinition.executeTransactionOnDispatcher(this)
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
    override suspend fun WritableDatabaseScope<DB>.execute(): Result<R> = try {
        ready?.invoke(this@Transaction)

        val result: R = if (shouldRunInTransaction) {
            db.executeTransactionOnDispatcher { transaction.run { this@execute.execute() } }
        } else {
            transaction.run { this@execute.execute() }
        }
        success?.invoke(this@Transaction, result)
        complete()
        Result.success(result)
    } catch (throwable: Throwable) {
        FlowLog.logError(throwable)
        val result: Result<R> = if (error != null) {
            error.invoke(this@Transaction, throwable)
            Result.failure(throwable)
        } else {
            Result.failure(
                RuntimeException(
                    "An exception occurred while executing a transaction",
                    throwable
                )
            )
        }
        complete()
        result
    }

    private fun complete() = completion?.invoke(this)

    fun newBuilder(): Builder<DB, R> {
        return Builder<DB, R>(transaction, databaseDefinition)
            .error(error)
            .success(success)
            .name(name)
            .shouldRunInTransaction(shouldRunInTransaction)
    }

    /**
     * The main entry point into [Transaction], this provides an easy way to build up transactions.
     */
    class Builder<DB : GeneratedDatabase, R : Any?>
    /**
     * @param transaction        The interface that actually executes the transaction.
     * @param database The database this transaction will run on. Should be the same
     * DB as the code that the transaction runs in.
     */
        (
        internal val transaction: SuspendableTransaction<DB, R>,
        internal val database: DB
    ) {
        internal var ready: Ready<DB, R>? = null
        internal var errorCallback: Error<DB, R>? = null
        internal var successCallback: Success<DB, R>? = null
        internal var completion: Completion<DB, R>? = null
        internal var name: String? = null
        internal var shouldRunInTransaction = true
        internal var runCallbacksOnSameThread: Boolean = false

        /**
         * Specify a callback when the transaction is ready to execute. Do an initialization here,
         * and cleanup on [completion]
         */
        fun ready(ready: Ready<DB, R>?) = apply {
            this.ready = ready
        }

        /**
         * Specify an error callback to return all and any [Throwable] that occurred during a [Transaction].
         * @param error Invoked on the UI thread, unless [runCallbacksOnSameThread] is true.
         */
        fun error(error: Error<DB, R>?) = apply {
            this.errorCallback = error
        }

        /**
         * Specify a listener for successful transactions. This is called when the [SuspendableTransaction]
         * specified is finished and it is posted on the UI thread.
         *
         * @param success The callback, invoked on the UI thread, unless [runCallbacksOnSameThread] is true.
         */
        fun success(success: Success<DB, R>?) = apply {
            this.successCallback = success
        }

        /**
         * Runs exactly once, no matter if it was successful or failed, at the end of the execution
         * of this transaction.
         * @param completion Invoked on the UI thread, unless [runCallbacksOnSameThread] is true.
         */
        fun completion(completion: Completion<DB, R>?) = apply {
            this.completion = completion
        }

        /**
         * Give this transaction a name.
         *
         * @param name The name of this transaction. Should be unique for any transaction currently
         * running in the [TransactionDispatcher].
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
        fun build(): Transaction<DB, R> = Transaction(this)

        /**
         * Convenience method to simply execute a transaction.
         */
        @JvmOverloads
        fun enqueue(
            ready: Ready<DB, R>? = null,
            error: Error<DB, R>? = null,
            completion: Completion<DB, R>? = null,
            success: Success<DB, R>? = null
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
            ready: Ready<DB, R>? = null,
            error: Error<DB, R>? = null,
            completion: Completion<DB, R>? = null,
            success: Success<DB, R>? = null
        ) =
            this.apply {
                ready?.let(this::ready)
                success?.let(this::success)
                error?.let(this::error)
                completion?.let(this::completion)
            }.build().execute()
    }

}

