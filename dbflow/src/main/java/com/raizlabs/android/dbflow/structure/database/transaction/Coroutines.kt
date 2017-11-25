package com.raizlabs.android.dbflow.structure.database.transaction

import com.raizlabs.android.dbflow.config.DatabaseDefinition
import com.raizlabs.android.dbflow.sql.queriable.Queriable
import com.raizlabs.android.dbflow.structure.delete
import com.raizlabs.android.dbflow.structure.insert
import com.raizlabs.android.dbflow.structure.load
import com.raizlabs.android.dbflow.structure.save
import com.raizlabs.android.dbflow.structure.update
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
    constructCoroutine(continuation, this) { queriableFunction(modelQueriable) }
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