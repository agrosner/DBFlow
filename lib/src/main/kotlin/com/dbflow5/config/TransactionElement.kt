package com.dbflow5.config

import kotlinx.coroutines.Job
import java.io.Closeable
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

/**
 * Description:
 */
internal class TransactionElement(
    private val transactionThreadControlJob: Job,
    internal val transactionDispatcher: ContinuationInterceptor
) : CoroutineContext.Element, Closeable {
    companion object Key : CoroutineContext.Key<TransactionElement>

    override val key: CoroutineContext.Key<TransactionElement>
        get() = TransactionElement

    /**
     * Number of transactions (including nested ones) started with this element.
     * Call [acquire] to increase the count and [release] to decrease it. If the count reaches zero
     * when [release] is invoked then the transaction job is cancelled and the transaction thread
     * is released.
     */
    private val referenceCount = AtomicInteger(0)
    fun acquire() {
        referenceCount.incrementAndGet()
    }

    fun release() {
        val count = referenceCount.decrementAndGet()
        if (count < 0) {
            throw IllegalStateException("Transaction was never started or was already released.")
        } else if (count == 0) {
            // Cancel the job that controls the transaction thread, causing it to be released.
            transactionThreadControlJob.cancel()
        }
    }

    override fun close() {
        release()
    }
}

internal inline fun <R> CoroutineContext.acquireTransaction(fn: TransactionElement.() -> R): R =
    this[TransactionElement]!!
        .apply { acquire() }
        .use { fn(it) }
