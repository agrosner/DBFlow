package com.raizlabs.android.dbflow.structure.database.transaction

import com.raizlabs.android.dbflow.config.DatabaseDefinition
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable
import kotlinx.coroutines.experimental.suspendCancellableCoroutine

/**
 * Description: Puts the [ModelQueriable] operation on the bg. inside the [queriableFunction] call
 *  the method you wish to execute within a transaction.
 */
suspend fun <T : Any, R> DatabaseDefinition.transact(modelQueriable: ModelQueriable<T>,
                                                     queriableFunction: (ModelQueriable<T>) -> R)
        = suspendCancellableCoroutine<R?> { continuation ->
    var value: R? = null
    val transaction = beginTransactionAsync { value = queriableFunction(modelQueriable) }
            .success {
                continuation.resume(value)
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