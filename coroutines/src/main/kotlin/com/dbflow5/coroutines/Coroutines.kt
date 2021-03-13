package com.dbflow5.coroutines

import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.query.Queriable
import com.dbflow5.structure.delete
import com.dbflow5.structure.insert
import com.dbflow5.structure.load
import com.dbflow5.structure.save
import com.dbflow5.structure.update
import com.dbflow5.transaction.FastStoreModelTransaction
import com.dbflow5.transaction.Transaction
import com.dbflow5.transaction.fastDelete
import com.dbflow5.transaction.fastInsert
import com.dbflow5.transaction.fastSave
import com.dbflow5.transaction.fastUpdate
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Turns this [Transaction.Builder] into a [Deferred] object to use in coroutines.
 */
fun <R : Any?> Transaction.Builder<R>.defer(): Deferred<R> {
    val deferred = CompletableDeferred<R>()
    val transaction = success { _, result -> deferred.complete(result) }
            .error { _, throwable -> deferred.completeExceptionally(throwable) }
            .build()
    deferred.invokeOnCompletion {
        if (deferred.isCancelled) {
            transaction.cancel()
        }
    }
    transaction.execute()
    return deferred
}

inline fun <R : Any?> constructCoroutine(continuation: CancellableContinuation<R>,
                                         databaseDefinition: DBFlowDatabase,
                                         crossinline fn: () -> R) {
    val transaction = databaseDefinition.beginTransactionAsync { fn() }
            .success { _, result -> continuation.resume(result) }
            .error { _, throwable ->
                if (continuation.isCancelled) return@error
                continuation.resumeWithException(throwable)
            }.build()
    transaction.execute()

    continuation.invokeOnCancellation {
        if (continuation.isCancelled) {
            transaction.cancel()
        }
    }
}


/**
 * Description: Puts this [Queriable] operation inside a coroutine. Inside the [queriableFunction]
 * execute the db operation.
 */
suspend inline fun <Q : Queriable, R : Any?> Q.awaitTransact(
        dbFlowDatabase: DBFlowDatabase,
        crossinline queriableFunction: Q.(DBFlowDatabase) -> R) = suspendCancellableCoroutine<R> { continuation ->
    com.dbflow5.coroutines.constructCoroutine(continuation, dbFlowDatabase) { queriableFunction(dbFlowDatabase) }
}

/**
 * Description: Puts a [Model] operation inside a coroutine. Inside the [queriableFunction]
 * execute the db operation.
 */
suspend inline fun <reified M : Any> M.awaitSave(databaseDefinition: DBFlowDatabase) = suspendCancellableCoroutine<Boolean> { continuation ->
    constructCoroutine(continuation, databaseDefinition) { save(databaseDefinition) }
}

/**
 * Description: Puts a [Model] operation inside a coroutine. Inside the [queriableFunction]
 * execute the db operation.
 */
suspend inline fun <reified M : Any> M.awaitInsert(databaseDefinition: DBFlowDatabase) = suspendCancellableCoroutine<Long> { continuation ->
    constructCoroutine(continuation, databaseDefinition) { insert(databaseDefinition) }
}

/**
 * Description: Puts a [Model] operation inside a coroutine. Inside the [queriableFunction]
 * execute the db operation.
 */
suspend inline fun <reified M : Any> M.awaitDelete(databaseDefinition: DBFlowDatabase) = suspendCancellableCoroutine<Boolean> { continuation ->
    constructCoroutine(continuation, databaseDefinition) { delete(databaseDefinition) }
}

/**
 * Description: Puts a [Model] operation inside a coroutine. Inside the [queriableFunction]
 * execute the db operation.
 */
suspend inline fun <reified M : Any> M.awaitUpdate(databaseDefinition: DBFlowDatabase) = suspendCancellableCoroutine<Boolean> { continuation ->
    constructCoroutine(continuation, databaseDefinition) { update(databaseDefinition) }
}

/**
 * Description: Puts a [Model] operation inside a coroutine. Inside the [queriableFunction]
 * execute the db operation.
 */
suspend inline fun <reified M : Any> M.awaitLoad(databaseDefinition: DBFlowDatabase) = suspendCancellableCoroutine<Unit> { continuation ->
    constructCoroutine(continuation, databaseDefinition) { load(databaseDefinition) }
}

/**
 * Description: Puts the [Collection] inside a [FastStoreModelTransaction] coroutine.
 */
suspend inline fun <reified T : Any, reified M : Collection<T>> M.awaitSave(databaseDefinition: DBFlowDatabase) = suspendCancellableCoroutine<Long> { continuation ->
    constructFastCoroutine(continuation, databaseDefinition) { fastSave() }
}

/**
 * Description: Puts the [Collection] inside a [FastStoreModelTransaction] coroutine.
 */
suspend inline fun <reified T : Any, reified M : Collection<T>> M.awaitInsert(databaseDefinition: DBFlowDatabase) = suspendCancellableCoroutine<Long> { continuation ->
    constructFastCoroutine(continuation, databaseDefinition) { fastInsert() }
}

/**
 * Description: Puts the [Collection] inside a [FastStoreModelTransaction] coroutine.
 */
suspend inline fun <reified T : Any, reified M : Collection<T>> M.awaitUpdate(databaseDefinition: DBFlowDatabase) = suspendCancellableCoroutine<Long> { continuation ->
    constructFastCoroutine(continuation, databaseDefinition) { fastUpdate() }
}

/**
 * Description: Puts the [Collection] inside a [FastStoreModelTransaction] coroutine.
 */
suspend inline fun <reified T : Any, reified M : Collection<T>> M.awaitDelete(databaseDefinition: DBFlowDatabase) = suspendCancellableCoroutine<Long> { continuation ->
    constructFastCoroutine(continuation, databaseDefinition) { fastDelete() }
}


inline fun <R : Any?> constructFastCoroutine(continuation: CancellableContinuation<Long>,
                                             databaseDefinition: DBFlowDatabase,
                                             crossinline fn: () -> FastStoreModelTransaction.Builder<R>) {
    val transaction = databaseDefinition.beginTransactionAsync(fn().build())
            .success { _, result -> continuation.resume(result) }
            .error { _, throwable ->
                if (continuation.isCancelled) return@error
                continuation.resumeWithException(throwable)
            }.build()
    transaction.execute()

    continuation.invokeOnCancellation {
        transaction.cancel()
    }
}