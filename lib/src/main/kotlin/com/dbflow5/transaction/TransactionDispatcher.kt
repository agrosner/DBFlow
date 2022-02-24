package com.dbflow5.transaction

import com.dbflow5.config.GeneratedDatabase
import com.dbflow5.config.TransactionElement
import com.dbflow5.config.acquireTransaction
import com.dbflow5.config.executeTransactionForResult
import com.dbflow5.database.scope.WritableDatabaseScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Job
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

fun interface SuspendableTransaction<DB : GeneratedDatabase, R> {
    suspend fun WritableDatabaseScope<DB>.execute(): R
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
    suspend fun <DB : GeneratedDatabase, R> executeTransaction(
        db: DB,
        transaction: SuspendableTransaction<DB, R>,
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
    override suspend fun <DB : GeneratedDatabase, R> executeTransaction(
        db: DB,
        transaction: SuspendableTransaction<DB, R>,
    ): R {
        // reuse transaction if nesting calls.
        return withContext(
            transactionContext(
                coroutineContext,
                if (transaction is Transaction<DB, *>) {
                    transaction.name
                } else null,
                transactionId = db.transactionId,
            )
        ) {
            coroutineContext.acquireTransaction {
                WritableDatabaseScope(db).executeTransactionForResult(transaction)
            }
        }
    }

}

/**
 * Reuses the [TransactionElement] if its within the same coroutineContext.
 */
internal suspend fun TransactionDispatcher.transactionContext(
    coroutineContext: CoroutineContext,
    coroutineName: String?,
    transactionId: ThreadLocal<Int>,
) = (coroutineContext[TransactionElement]?.transactionDispatcher
    ?: createTransactionContext(coroutineName, transactionId))

private suspend fun TransactionDispatcher.createTransactionContext(
    coroutineName: String?,
    transactionId: ThreadLocal<Int>,
): CoroutineContext {
    val controlJob = Job()
    // make sure to tie the control job to this context to avoid blocking the transaction if
    // context get cancelled before we can even start using this job. Otherwise, the acquired
    // transaction thread will forever wait for the controlJob to be cancelled.
    // see b/148181325
    coroutineContext[Job]?.invokeOnCompletion {
        controlJob.cancel()
    }
    val element = TransactionElement(controlJob, dispatcher)
    val threadLocalElement = transactionId.asContextElement(System.identityHashCode(controlJob))
    val context = dispatcher + element + threadLocalElement
    //
    if (!coroutineName.isNullOrEmpty()) {
        return context + CoroutineName(coroutineName)
    }
    return context
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
