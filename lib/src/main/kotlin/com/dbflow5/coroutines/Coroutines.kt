package com.dbflow5.coroutines

import com.dbflow5.transaction.Transaction
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

/**
 * Turns this [Transaction.Builder] into a [Deferred] object to use in coroutines.
 */
fun <R : Any?> Transaction.Builder<*, R>.defer(): Deferred<R> {
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
