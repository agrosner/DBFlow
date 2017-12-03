package com.raizlabs.dbflow5.coroutines

import com.raizlabs.dbflow5.config.DatabaseDefinition
import com.raizlabs.dbflow5.query.Queriable
import com.raizlabs.dbflow5.structure.delete
import com.raizlabs.dbflow5.structure.insert
import com.raizlabs.dbflow5.structure.load
import com.raizlabs.dbflow5.structure.save
import com.raizlabs.dbflow5.structure.update
import com.raizlabs.dbflow5.transaction.FastStoreModelTransaction
import com.raizlabs.dbflow5.transaction.fastDelete
import com.raizlabs.dbflow5.transaction.fastInsert
import com.raizlabs.dbflow5.transaction.fastSave
import com.raizlabs.dbflow5.transaction.fastUpdate
import kotlinx.coroutines.experimental.CancellableContinuation
import kotlinx.coroutines.experimental.suspendCancellableCoroutine

/**
 * Description: Puts this [Queriable] operation inside a coroutine. Inside the [queriableFunction]
 * execute the db operation.
 */
inline suspend fun <Q : Queriable, R : Any?> DatabaseDefinition.transact(
        modelQueriable: Q,
        crossinline queriableFunction: Q.() -> R)
        = suspendCancellableCoroutine<R> { continuation ->
    com.raizlabs.dbflow5.coroutines.constructCoroutine(continuation, this) { queriableFunction(modelQueriable) }
}

inline fun <R : Any?> constructCoroutine(continuation: CancellableContinuation<R>,
                                         databaseDefinition: DatabaseDefinition,
                                         crossinline fn: () -> R) {
    val transaction = databaseDefinition.beginTransactionAsync { fn() }
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
        = suspendCancellableCoroutine<Boolean> { continuation ->
    constructCoroutine(continuation, databaseDefinition) { save(databaseDefinition) }
}

/**
 * Description: Puts a [Model] operation inside a coroutine. Inside the [queriableFunction]
 * execute the db operation.
 */
inline suspend fun <reified M : Any> M.awaitInsert(databaseDefinition: DatabaseDefinition)
        = suspendCancellableCoroutine<Long> { continuation ->
    constructCoroutine(continuation, databaseDefinition) { insert(databaseDefinition) }
}

/**
 * Description: Puts a [Model] operation inside a coroutine. Inside the [queriableFunction]
 * execute the db operation.
 */
inline suspend fun <reified M : Any> M.awaitDelete(databaseDefinition: DatabaseDefinition)
        = suspendCancellableCoroutine<Boolean> { continuation ->
    constructCoroutine(continuation, databaseDefinition) { delete(databaseDefinition) }
}

/**
 * Description: Puts a [Model] operation inside a coroutine. Inside the [queriableFunction]
 * execute the db operation.
 */
inline suspend fun <reified M : Any> M.awaitUpdate(databaseDefinition: DatabaseDefinition)
        = suspendCancellableCoroutine<Boolean> { continuation ->
    constructCoroutine(continuation, databaseDefinition) { update(databaseDefinition) }
}

/**
 * Description: Puts a [Model] operation inside a coroutine. Inside the [queriableFunction]
 * execute the db operation.
 */
inline suspend fun <reified M : Any> M.awaitLoad(databaseDefinition: DatabaseDefinition)
        = suspendCancellableCoroutine<Unit> { continuation ->
    constructCoroutine(continuation, databaseDefinition) { load(databaseDefinition) }
}

/**
 * Description: Puts the [Collection] inside a [FastStoreModelTransaction] coroutine.
 */
inline suspend fun <reified T : Any, reified M : Collection<T>> M.awaitSave(databaseDefinition: DatabaseDefinition)
        = suspendCancellableCoroutine<Long> { continuation ->
    constructFastCoroutine(continuation, databaseDefinition) { fastSave() }
}

/**
 * Description: Puts the [Collection] inside a [FastStoreModelTransaction] coroutine.
 */
inline suspend fun <reified T : Any, reified M : Collection<T>> M.awaitInsert(databaseDefinition: DatabaseDefinition)
        = suspendCancellableCoroutine<Long> { continuation ->
    constructFastCoroutine(continuation, databaseDefinition) { fastInsert() }
}

/**
 * Description: Puts the [Collection] inside a [FastStoreModelTransaction] coroutine.
 */
inline suspend fun <reified T : Any, reified M : Collection<T>> M.awaitUpdate(databaseDefinition: DatabaseDefinition)
        = suspendCancellableCoroutine<Long> { continuation ->
    constructFastCoroutine(continuation, databaseDefinition) { fastUpdate() }
}

/**
 * Description: Puts the [Collection] inside a [FastStoreModelTransaction] coroutine.
 */
inline suspend fun <reified T : Any, reified M : Collection<T>> M.awaitDelete(databaseDefinition: DatabaseDefinition)
        = suspendCancellableCoroutine<Long> { continuation ->
    constructFastCoroutine(continuation, databaseDefinition) { fastDelete() }
}


inline fun <R : Any?> constructFastCoroutine(continuation: CancellableContinuation<Long>,
                                             databaseDefinition: DatabaseDefinition,
                                             crossinline fn: () -> FastStoreModelTransaction.Builder<R>) {
    val transaction = databaseDefinition.beginTransactionAsync(fn().build())
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