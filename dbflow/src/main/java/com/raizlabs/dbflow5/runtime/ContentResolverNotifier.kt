package com.raizlabs.dbflow5.runtime

import android.content.ContentResolver
import com.raizlabs.dbflow5.adapter.ModelAdapter
import com.raizlabs.dbflow5.config.FlowManager
import com.raizlabs.dbflow5.getNotificationUri
import com.raizlabs.dbflow5.query.SQLOperator
import com.raizlabs.dbflow5.structure.ChangeAction

/**
 * The default use case, it notifies via the [ContentResolver] system.
 *
 * @param [authority] Specify the content URI authority you wish to use here. This will get propagated
 * everywhere that changes get called from in a specific database.
 */
class ContentResolverNotifier(val authority: String) : ModelNotifier {

    override fun <T : Any> notifyModelChanged(model: T, adapter: ModelAdapter<T>,
                                              action: ChangeAction) {
        if (FlowContentObserver.shouldNotify()) {
            FlowManager.context.contentResolver
                .notifyChange(getNotificationUri(authority,
                    adapter.table, action,
                    adapter.getPrimaryConditionClause(model).conditions),
                    null, true)
        }
    }

    override fun <T : Any> notifyTableChanged(table: Class<T>, action: ChangeAction) {
        if (FlowContentObserver.shouldNotify()) {
            FlowManager.context.contentResolver
                .notifyChange(getNotificationUri(authority, table, action,
                    null as Array<SQLOperator>?), null, true)
        }
    }

    override fun newRegister(): TableNotifierRegister = FlowContentTableNotifierRegister(authority)

    class FlowContentTableNotifierRegister(contentAuthority: String) : TableNotifierRegister {

        private val flowContentObserver = FlowContentObserver(contentAuthority)

        private var tableChangedListener: OnTableChangedListener? = null

        private val internalContentChangeListener = object : OnTableChangedListener {
            override fun onTableChanged(table: Class<*>?, action: ChangeAction) {
                tableChangedListener?.onTableChanged(table, action)
            }
        }

        init {
            flowContentObserver.addOnTableChangedListener(internalContentChangeListener)
        }

        override fun <T> register(tClass: Class<T>) {
            flowContentObserver.registerForContentChanges(FlowManager.context, tClass)
        }

        override fun <T> unregister(tClass: Class<T>) {
            flowContentObserver.unregisterForContentChanges(FlowManager.context)
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
