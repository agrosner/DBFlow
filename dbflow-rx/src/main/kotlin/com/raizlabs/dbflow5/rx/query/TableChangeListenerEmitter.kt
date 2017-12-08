package com.raizlabs.dbflow5.rx.query

import com.raizlabs.dbflow5.config.FlowManager
import com.raizlabs.dbflow5.query.From
import com.raizlabs.dbflow5.query.Join
import com.raizlabs.dbflow5.query.ModelQueriable
import com.raizlabs.dbflow5.query.Where
import com.raizlabs.dbflow5.runtime.OnTableChangedListener
import com.raizlabs.dbflow5.runtime.TableNotifierRegister
import com.raizlabs.dbflow5.structure.ChangeAction
import rx.Emitter
import rx.Subscription
import rx.functions.Action1

/**
 * Description: Emits when table changes occur for the related table on the [ModelQueriable].
 * If the [ModelQueriable] relates to a [Join], this can be multiple tables.
 */
class TableChangeListenerEmitter<TModel : Any>(private val modelQueriable: ModelQueriable<TModel>)
    : Action1<Emitter<ModelQueriable<TModel>>> {

    override fun call(modelQueriableEmitter: Emitter<ModelQueriable<TModel>>) {
        modelQueriableEmitter.setSubscription(
                FlowContentObserverSubscription(modelQueriableEmitter, modelQueriable.table))
    }

    private inner class FlowContentObserverSubscription internal constructor(
            private val modelQueriableEmitter: Emitter<ModelQueriable<TModel>>, table: Class<TModel>)
        : Subscription {

        private val register: TableNotifierRegister = FlowManager.newRegisterForTable(table)

        private val onTableChangedListener = object : OnTableChangedListener {
            override fun onTableChanged(table: Class<*>?, action: ChangeAction) {
                if (modelQueriable.table == table) {
                    modelQueriableEmitter.onNext(modelQueriable)
                }
            }
        }

        init {
            val from: From<TModel>? = when {
                modelQueriable is From<*> -> modelQueriable as From<TModel>
                modelQueriable is Where<*> && modelQueriable.whereBase is From<*> ->
                    @Suppress("UNCHECKED_CAST")
                    modelQueriable.whereBase as From<TModel>
                else -> null
            }

            // From could be part of many joins, so we register for all affected tables here.
            if (from != null) {
                from.associatedTables.forEach { register.register(it) }
            } else {
                register.register(table)
            }

            register.setListener(onTableChangedListener)
        }

        override fun unsubscribe() {
            register.unregisterAll()
        }

        override fun isUnsubscribed(): Boolean {
            return !register.isSubscribed
        }
    }
}
