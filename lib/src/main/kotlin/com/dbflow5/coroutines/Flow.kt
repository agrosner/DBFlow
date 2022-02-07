package com.dbflow5.coroutines

import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.config.enqueueTransaction
import com.dbflow5.observing.OnTableChangedObserver
import com.dbflow5.query2.ExecutableQuery
import com.dbflow5.query2.HasAssociatedAdapters
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
fun <Result, Q> Q.toFlow(
    db: DBFlowDatabase,
): Flow<Result> where Q : ExecutableQuery<Result>, Q : HasAssociatedAdapters {
    return callbackFlow {
        fun evaluateEmission() {
            db.enqueueTransaction { channel.send(execute()) }
        }

        val onTableChangedObserver =
            object : OnTableChangedObserver(associatedAdapters.map { it.table }.toList()) {
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
fun <DB : DBFlowDatabase, R : Any?> Transaction.Builder<DB, R>.toFlow(): Flow<R> {
    return callbackFlow {
        val transaction = success { _, r -> channel.trySend(r) }
            .error { _, throwable -> channel.close(throwable) }
            .completion { channel.close() }
            .build()
        transaction.enqueue()
        awaitClose { transaction.cancel() }
    }
}