package com.raizlabs.android.dbflow.structure.database.transaction

import com.raizlabs.android.dbflow.config.DatabaseDefinition
import com.raizlabs.android.dbflow.sql.queriable.Queriable
import kotlinx.coroutines.experimental.CancellableContinuation
import kotlinx.coroutines.experimental.suspendCancellableCoroutine

/**
 * Description: Puts this [Queriable] operation inside a coroutine. Inside the [queriableFunction]
 * execute the db operation.
 */
suspend fun <Q : Queriable, R> Q.transact(databaseDefinition: DatabaseDefinition,
                                          queriableFunction: Q.() -> R)
        = suspendCancellableCoroutine { continuation: CancellableContinuation<R> ->
    var value: R? = null
    val transaction = databaseDefinition.beginTransactionAsync { value = queriableFunction() }
            .success {
                continuation.resume(value!!)
            }
            .error { _, throwable ->
                if (continuation.isCancelled) return@error
                continuation.resumeWithException(throwable)
            }.build()
    transaction.execute()

    continuation.invokeOnCompletion {
        if (continuation.isCancelled) {
            transaction.cancel()
        }
    }
}