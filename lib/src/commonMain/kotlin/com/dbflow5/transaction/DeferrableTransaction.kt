package com.dbflow5.transaction

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

/**
 * Defers and calls [Transaction.enqueue].
 */
fun <R : Any?> Transaction.Builder<*, R>.deferAsync(): Deferred<R> {
    val deferred = CompletableDeferred<R>()
    val transaction = success { _, result -> deferred.complete(result) }
        .error { _, throwable -> deferred.completeExceptionally(throwable) }
        .build()
    deferred.invokeOnCompletion {
        if (deferred.isCancelled) {
            transaction.cancel()
        }
    }
    transaction.enqueue()
    return deferred
}
