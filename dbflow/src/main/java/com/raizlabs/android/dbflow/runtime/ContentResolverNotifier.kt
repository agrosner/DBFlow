package com.raizlabs.android.dbflow.runtime

import android.content.ContentResolver

import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.sql.getNotificationUri
import com.raizlabs.android.dbflow.sql.language.SQLOperator
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.structure.ModelAdapter

/**
 * The default use case, it notifies via the [ContentResolver] system.
 */
class ContentResolverNotifier : ModelNotifier {

    override fun <T : Any> notifyModelChanged(model: T, adapter: ModelAdapter<T>,
                                              action: BaseModel.Action) {
        if (FlowContentObserver.shouldNotify()) {
            FlowManager.context.contentResolver
                    .notifyChange(getNotificationUri(adapter.modelClass, action,
                            adapter.getPrimaryConditionClause(model).conditions), null, true)
        }
    }

    override fun <T : Any> notifyTableChanged(table: Class<T>, action: BaseModel.Action) {
        if (FlowContentObserver.shouldNotify()) {
            FlowManager.context.contentResolver
                    .notifyChange(getNotificationUri(table, action,
                            null as Array<SQLOperator>?), null, true)
        }
    }

    override fun newRegister(): TableNotifierRegister = FlowContentTableNotifierRegister()

    class FlowContentTableNotifierRegister : TableNotifierRegister {

        private val flowContentObserver = FlowContentObserver()

        private var tableChangedListener: OnTableChangedListener? = null

        private val internalContentChangeListener = object : OnTableChangedListener {
            override fun onTableChanged(table: Class<*>?, action: BaseModel.Action) {
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
