package com.dbflow5.runtime

import android.content.ContentResolver
import android.content.Context
import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.getNotificationUri
import com.dbflow5.query2.operations.BaseOperator
import com.dbflow5.structure.ChangeAction
import kotlin.reflect.KClass

/**
 * The default use case, it notifies via the [ContentResolver] system.
 *
 * @param [authority] Specify the content URI authority you wish to use here. This will get propagated
 * everywhere that changes get called from in a specific database.
 */
class ContentResolverNotifier(
    private val context: Context,
    val authority: String,
    override val db: DBFlowDatabase,
) : ModelNotifier {

    override fun <T : Any> notifyModelChanged(
        model: T, adapter: ModelAdapter<T>,
        action: ChangeAction
    ) {
        if (FlowContentObserver.shouldNotify()) {
            context.contentResolver.notifyChange(
                getNotificationUri(
                    authority, adapter.table, action,
                    adapter.getPrimaryConditionClause(model).operations
                        .filterIsInstance<BaseOperator.SingleValueOperator<Any?>>()
                ), null, true
            )
        }
    }

    override fun <T : Any> notifyTableChanged(table: KClass<T>, action: ChangeAction) {
        if (FlowContentObserver.shouldNotify()) {
            context.contentResolver.notifyChange(
                getNotificationUri(
                    authority,
                    table,
                    action,
                    null as Array<BaseOperator.SingleValueOperator<Any?>>?
                ),
                null,
                true
            )
        }
    }

    override fun newRegister(): TableNotifierRegister =
        FlowContentTableNotifierRegister(context, authority)

    class FlowContentTableNotifierRegister(private val context: Context, contentAuthority: String) :
        TableNotifierRegister {

        private val flowContentObserver = FlowContentObserver(contentAuthority)

        private var tableChangedListener: OnTableChangedListener? = null

        private val internalContentChangeListener =
            OnTableChangedListener { table, action ->
                tableChangedListener?.onTableChanged(
                    table,
                    action
                )
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
