package com.raizlabs.dbflow5.rx2.query

import com.raizlabs.dbflow5.config.FlowManager
import com.raizlabs.dbflow5.config.databaseForTable
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.query.Join
import com.raizlabs.dbflow5.query.ModelQueriable
import com.raizlabs.dbflow5.query.extractFrom
import com.raizlabs.dbflow5.runtime.OnTableChangedListener
import com.raizlabs.dbflow5.runtime.TableNotifierRegister
import com.raizlabs.dbflow5.rx2.transaction.asMaybe
import com.raizlabs.dbflow5.structure.ChangeAction
import io.reactivex.FlowableEmitter
import io.reactivex.FlowableOnSubscribe
import io.reactivex.disposables.Disposables

/**
 * Description: Emits when table changes occur for the related table on the [ModelQueriable].
 * If the [ModelQueriable] relates to a [Join], this can be multiple tables.
 */
class TableChangeOnSubscribe<T : Any, R : Any?>(private val modelQueriable: ModelQueriable<T>,
                                                private val evalFn: (DatabaseWrapper, ModelQueriable<T>) -> R)
    : FlowableOnSubscribe<R> {

    private val register: TableNotifierRegister = FlowManager.newRegisterForTable(modelQueriable.table)
    private lateinit var flowableEmitter: FlowableEmitter<R>

    private val onTableChangedListener = object : OnTableChangedListener {
        override fun onTableChanged(table: Class<*>?, action: ChangeAction) {
            if (modelQueriable.table == table) {
                evaluateEmission()
            }
        }
    }

    private fun evaluateEmission() {
        if (this::flowableEmitter.isInitialized) {
            databaseForTable(modelQueriable.table.kotlin)
                .beginTransactionAsync { evalFn(it, modelQueriable) }
                .asMaybe()
                .subscribe {
                    flowableEmitter.onNext(it)
                }
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(Exception::class)
    override fun subscribe(e: FlowableEmitter<R>) {
        flowableEmitter = e
        e.setDisposable(Disposables.fromRunnable { register.unregisterAll() })

        // From could be part of many joins, so we register for all affected tables here.
        val from = modelQueriable.extractFrom()
        if (from != null) {
            val associatedTables = from.associatedTables
            for (table in associatedTables) {
                register.register(table)
            }
        } else {
            register.register(modelQueriable.table)
        }

        register.setListener(onTableChangedListener)
        evaluateEmission()
    }

}
