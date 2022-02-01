package com.dbflow5.coroutines

import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.observing.OnTableChangedObserver
import com.dbflow5.query.ModelQueriable
import com.dbflow5.query.ModelQueriableEvalFn
import com.dbflow5.query.extractFrom
import com.dbflow5.transaction.Transaction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.reflect.KClass

/**
 * Builds a [Flow] that listens for table changes and emits when they change.
 */
@ExperimentalCoroutinesApi
fun <T : Any, R : Any?> ModelQueriable<T>.toFlow(
    db: DBFlowDatabase,
    evalFn: ModelQueriableEvalFn<T, R>
): Flow<R> {
    return callbackFlow {
        val tables = extractFrom()?.associatedTables ?: setOf(table)
        fun evaluateEmission() {
            db.enqueueTransaction {
                channel.send(evalFn(it))
            }
        }

        val onTableChangedObserver = object : OnTableChangedObserver(tables.toList()) {
            override fun onChanged(tables: Set<KClass<*>>) {
                evaluateEmission()
            }
        }

        // force initialize the db
        db.writableDatabase

        val observer = db.tableObserver
        observer.addOnTableChangedObserver(onTableChangedObserver)

        // trigger initial emission on active.
        evaluateEmission()

        awaitClose { observer.removeOnTableChangedObserver(onTableChangedObserver) }
    }
}

/**
 * Turns a single [Transaction.Builder] into a cold [Flow] object. This is a one-shot
 * [Flow] that will close itself when transacion executes.
 */
@ExperimentalCoroutinesApi
fun <R : Any?> Transaction.Builder<R>.toFlow(): Flow<R> {
    return callbackFlow {
        val transaction = success { _, r -> channel.trySend(r) }
            .error { _, throwable -> channel.close(throwable) }
            .completion { channel.close() }
            .build()
        transaction.enqueue()
        awaitClose { transaction.cancel() }
    }
}