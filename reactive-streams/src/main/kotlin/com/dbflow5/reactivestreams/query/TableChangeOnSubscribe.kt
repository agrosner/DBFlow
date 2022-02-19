package com.dbflow5.reactivestreams.query

import com.dbflow5.config.GeneratedDatabase
import com.dbflow5.config.beginTransactionAsync
import com.dbflow5.observing.OnTableChangedObserver
import com.dbflow5.query.ExecutableQuery
import com.dbflow5.query.HasAssociatedAdapters
import com.dbflow5.query.SelectResult
import com.dbflow5.reactivestreams.transaction.asSingle
import io.reactivex.rxjava3.core.FlowableEmitter
import io.reactivex.rxjava3.core.FlowableOnSubscribe
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import kotlin.reflect.KClass

/**
 * Description: Emits when table changes occur for the related table on the [SelectResult].
 * If the [executable] relates to a Join, this can be multiple tables.
 */
class TableChangeOnSubscribe<Table : Any, Result : Any, Q>(
    private val executable: Q,
    private val selectResultFn: suspend SelectResult<Table>.() -> Result,
    private val db: GeneratedDatabase,
) : FlowableOnSubscribe<Result>
    where Q : ExecutableQuery<SelectResult<Table>>,
          Q : HasAssociatedAdapters {

    private lateinit var flowableEmitter: FlowableEmitter<Result>

    private val currentTransactions = CompositeDisposable()

    private val associatedTables: Set<KClass<*>> = executable.associatedAdapters.map { it.table }
        .toSet()

    private val onTableChangedObserver =
        object : OnTableChangedObserver(associatedTables.toList()) {
            override fun onChanged(tables: Set<KClass<*>>) {
                evaluateEmission()
            }
        }

    private fun evaluateEmission() {
        if (this::flowableEmitter.isInitialized) {
            currentTransactions.add(
                db
                    .beginTransactionAsync { executable.execute().selectResultFn() }
                    .shouldRunInTransaction(false)
                    .asSingle()
                    .subscribe { result -> flowableEmitter.onNext(result) }
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(Exception::class)
    override fun subscribe(e: FlowableEmitter<Result>) {
        flowableEmitter = e

        // force initialize the dbr
        db.writableDatabase

        val observer = db.tableObserver
        e.setDisposable(Disposable.fromRunnable {
            observer.removeOnTableChangedObserver(onTableChangedObserver)
            currentTransactions.dispose()
        })

        observer.addOnTableChangedObserver(onTableChangedObserver)

        // emit once on subscribe.
        evaluateEmission()
    }

}
