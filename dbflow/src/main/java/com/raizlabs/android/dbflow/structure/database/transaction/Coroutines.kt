package com.raizlabs.android.dbflow.structure.database.transaction

import com.raizlabs.android.dbflow.config.DatabaseDefinition
import com.raizlabs.android.dbflow.config.modelAdapter
import com.raizlabs.android.dbflow.sql.queriable.Queriable
import kotlinx.coroutines.experimental.CancellableContinuation
import kotlinx.coroutines.experimental.suspendCancellableCoroutine

/**
 * Description: Puts this [Queriable] operation inside a coroutine. Inside the [queriableFunction]
 * execute the db operation.
 */
suspend fun <Q : Queriable, R : Any> Q.transact(databaseDefinition: DatabaseDefinition,
                                                queriableFunction: Q.() -> R)
        = suspendCancellableCoroutine { continuation: CancellableContinuation<R> ->
    val transaction = databaseDefinition.beginTransactionAsync { queriableFunction() }
            .success { _, result -> continuation.resume(result) }
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


/**
 * Description: Puts a [Model] operation inside a coroutine. Inside the [queriableFunction]
 * execute the db operation.
 */
inline suspend fun <reified M : Any> M.awaitSave(databaseDefinition: DatabaseDefinition)
        = suspendCancellableCoroutine { continuation: CancellableContinuation<Boolean> ->
    val transaction = databaseDefinition.beginTransactionAsync {
        modelAdapter<M>().save(this, databaseDefinition)
    }
            .success { _, result -> continuation.resume(result) }
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