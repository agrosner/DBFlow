package com.dbflow5.coroutines

import com.dbflow5.config.GeneratedDatabase
import com.dbflow5.config.enqueueTransaction
import com.dbflow5.observing.OnTableChangedObserver
import com.dbflow5.query.ExecutableQuery
import com.dbflow5.query.HasAssociatedAdapters
import com.dbflow5.query.SelectResult
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
fun <Table : Any, Result, Q> Q.toFlow(
    db: GeneratedDatabase,
    /**
     * If true, we evaluate eagerly the query on collection. This may not be desired
     * when retrieving non-null [SelectResult.single] operations if no results are found.
     */
    runQueryOnCollect: Boolean = true,
    selectResultFn: suspend SelectResult<Table>.() -> Result
): Flow<Result>
    where Q : ExecutableQuery<SelectResult<Table>>,
          Q : HasAssociatedAdapters {
    return callbackFlow {
        fun evaluateEmission() {
            db.enqueueTransaction { channel.send(execute().selectResultFn()) }
        }

        val onTableChangedObserver =
            object : OnTableChangedObserver(associatedAdapters.map { it.type }.toList()) {
                override fun onChanged(tables: Set<KClass<*>>) {
                    evaluateEmission()
                }
            }

        // force initialize the db
        db.writableDatabase

        val observer = db.tableObserver
        observer.addOnTableChangedObserver(onTableChangedObserver)

        // trigger initial emission on active.
        if (runQueryOnCollect) {
            evaluateEmission()
        }

        awaitClose { observer.removeOnTableChangedObserver(onTableChangedObserver) }
    }
}

/**
 * Turns a single [Transaction.Builder] into a cold [Flow] object. This is a one-shot
 * [Flow] that will close itself when transacion executes.
 */
@ExperimentalCoroutinesApi
fun <DB : GeneratedDatabase, R : Any?> Transaction.Builder<DB, R>.toFlow(): Flow<R> {
    return callbackFlow {
        val transaction = success { _, r -> channel.trySend(r) }
            .error { _, throwable -> channel.close(throwable) }
            .completion { channel.close() }
            .build()
        transaction.enqueue()
        awaitClose { transaction.cancel() }
    }
}