package com.dbflow5.transaction

import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.config.TransactionElement
import com.dbflow5.config.acquireTransaction
import com.dbflow5.database.DatabaseWrapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

fun interface SuspendableTransaction<R> {
    suspend fun execute(db: DatabaseWrapper): R
}

/**
 * Manages Async Transaction calls.
 *
 * The default usage creates a single-threaded executor. One per database. Avoid
 * using multiple threads for DB operations as DB access locks when used on multiple threads at
 * same time.
 */
interface TransactionDispatcher {
    /**
     * The dispatcher to run transactions on.
     */
    val dispatcher: CoroutineDispatcher

    /**
     * Executes the transaction and suspends until result is returned.
     */
    suspend fun <R> executeTransaction(
        db: DBFlowDatabase,
        transaction: SuspendableTransaction<R>,
    ): R
}

/**
 * Runs all operations in a single transaction. Will attempt to reuse transaction when nesting
 * multiple ones.
 */
internal class DefaultTransactionDispatcher(
    override val dispatcher: CoroutineDispatcher,
) : TransactionDispatcher {

    /**
     * Runs the transaction within the [dispatcher]
     */
    override suspend fun <R> executeTransaction(
        db: DBFlowDatabase,
        transaction: SuspendableTransaction<R>,
    ): R {
        // reuse transaction if nesting calls.
        return withContext(transactionContext(coroutineContext)) {
            coroutineContext.acquireTransaction {
                db.executeTransactionForResult(transaction)
            }
        }
    }

}

/**
 * Reuses the [TransactionElement] if its within the same coroutineContext.
 */
internal suspend fun TransactionDispatcher.transactionContext(
    coroutineContext: CoroutineContext
) = (coroutineContext[TransactionElement]?.transactionDispatcher
    ?: createTransactionContext())

private suspend fun TransactionDispatcher.createTransactionContext(): CoroutineContext {
    val controlJob = Job()
    // make sure to tie the control job to this context to avoid blocking the transaction if
    // context get cancelled before we can even start using this job. Otherwise, the acquired
    // transaction thread will forever wait for the controlJob to be cancelled.
    // see b/148181325
    coroutineContext[Job]?.invokeOnCompletion {
        controlJob.cancel()
    }
    val element = TransactionElement(controlJob, dispatcher)
    return coroutineContext + element
}

/**
 * Creates [TransactionDispatcher] with a single thread.
 */
fun TransactionDispatcher(
): TransactionDispatcher = DefaultTransactionDispatcher(
    Executors.newSingleThreadExecutor().asCoroutineDispatcher(),
)

/**
 * Creates [TransactionDispatcher] with the specified dispatcher.
 */
fun TransactionDispatcher(dispatcher: CoroutineDispatcher): TransactionDispatcher =
    DefaultTransactionDispatcher(dispatcher)
