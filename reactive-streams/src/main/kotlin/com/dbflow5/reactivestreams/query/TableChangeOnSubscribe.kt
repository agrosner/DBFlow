package com.dbflow5.reactivestreams.query

import com.dbflow5.config.FlowManager
import com.dbflow5.config.databaseForTable
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.observing.OnTableChangedObserver
import com.dbflow5.query.Join
import com.dbflow5.query.ModelQueriable
import com.dbflow5.query.extractFrom
import com.dbflow5.reactivestreams.transaction.asMaybe
import io.reactivex.rxjava3.core.FlowableEmitter
import io.reactivex.rxjava3.core.FlowableOnSubscribe
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import kotlin.reflect.KClass

/**
 * Description: Emits when table changes occur for the related table on the [ModelQueriable].
 * If the [ModelQueriable] relates to a [Join], this can be multiple tables.
 */
class TableChangeOnSubscribe<T : Any, R : Any?>(private val modelQueriable: ModelQueriable<T>,
                                                private val evalFn: ModelQueriable<T>.(DatabaseWrapper) -> R)
    : FlowableOnSubscribe<R> {

    private lateinit var flowableEmitter: FlowableEmitter<R>

    private val currentTransactions = CompositeDisposable()

    private val associatedTables: Set<Class<*>> = modelQueriable.extractFrom()?.associatedTables
        ?: setOf(modelQueriable.table)

    private val onTableChangedObserver = object : OnTableChangedObserver(associatedTables.toList()) {
        override fun onChanged(tables: Set<Class<*>>) {
            if (tables.isNotEmpty()) {
                evaluateEmission(tables.first().kotlin)
            }
        }
    }

    private fun evaluateEmission(table: KClass<*> = modelQueriable.table.kotlin) {
        if (this::flowableEmitter.isInitialized) {
            currentTransactions.add(databaseForTable(table)
                .beginTransactionAsync { modelQueriable.evalFn(it) }
                .shouldRunInTransaction(false)
                .asMaybe()
                .subscribe {
                    flowableEmitter.onNext(it)
                })
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(Exception::class)
    override fun subscribe(e: FlowableEmitter<R>) {
        flowableEmitter = e

        val db = FlowManager.getDatabaseForTable(associatedTables.first())
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
