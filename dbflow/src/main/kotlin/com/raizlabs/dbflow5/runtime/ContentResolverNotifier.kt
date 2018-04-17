package com.raizlabs.dbflow5.runtime

import android.content.ContentResolver
import android.content.Context
import kotlin.reflect.KClass
import com.raizlabs.dbflow5.adapter.ModelAdapter
import com.raizlabs.dbflow5.getNotificationUri
import com.raizlabs.dbflow5.query.SQLOperator
import com.raizlabs.dbflow5.structure.ChangeAction

/**
 * The default use case, it notifies via the [ContentResolver] system.
 *
 * @param [authority] Specify the content URI authority you wish to use here. This will get propagated
 * everywhere that changes get called from in a specific database.
 */
class ContentResolverNotifier(private val context: Context,
                              val authority: String) : ModelNotifier {

    override fun <T : Any> notifyModelChanged(model: T, adapter: ModelAdapter<T>,
                                              action: ChangeAction) {
        if (FlowContentObserver.shouldNotify()) {
            context.contentResolver.notifyChange(
                getNotificationUri(authority, adapter.table, action,
                    adapter.getPrimaryConditionClause(model).conditions), null, true)
        }
    }

    override fun <T : Any> notifyTableChanged(table: KClass<T>, action: ChangeAction) {
        if (FlowContentObserver.shouldNotify()) {
            context.contentResolver.notifyChange(
                getNotificationUri(authority, table, action, null as Array<SQLOperator>?), null, true)
        }
    }

    override fun newRegister(): TableNotifierRegister = FlowContentTableNotifierRegister(context, authority)

    class FlowContentTableNotifierRegister(private val context: Context, contentAuthority: String) : TableNotifierRegister {

        private val flowContentObserver = FlowContentObserver(contentAuthority)

        private var tableChangedListener: OnTableChangedListener? = null

        private val internalContentChangeListener = object : OnTableChangedListener {
            override fun onTableChanged(table: KClass<*>?, action: ChangeAction) {
                tableChangedListener?.onTableChanged(table, action)
            }
        }

        init {
            flowContentObserver.addOnTableChangedListener(internalContentChangeListener)
        }

        override fun <T : Any> register(tClass: KClass<T>) {
            flowContentObserver.registerForContentChanges(context, tClass)
        }

        override fun <T : Any> unregister(tClass: KClass<T>) {
            flowContentObserver.unregisterForContentChanges(context)
        }

        override fun unregisterAll() {
            flowContentObserver.removeTableChangedListener(internalContentChangeListener)
            this.tableChangedListener = null
        }

        override fun setListener(listener: OnTableChangedListener?) {
            this.tableChangedListener = listener
        }

        override val isSubscribed: Boolean
            get() = !flowContentObserver.isSubscribed
    }
}
