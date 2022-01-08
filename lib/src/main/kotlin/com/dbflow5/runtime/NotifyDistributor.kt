package com.dbflow5.runtime

import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.config.FlowManager
import com.dbflow5.structure.ChangeAction
import kotlin.reflect.KClass

/**
 * Description: Distributes notifications to the [ModelNotifier].
 */
class NotifyDistributor : ModelNotifier {

    override fun newRegister(): TableNotifierRegister {
        throw RuntimeException("Cannot create a register from the distributor class")
    }

    override fun <T : Any> notifyModelChanged(
        model: T,
        adapter: ModelAdapter<T>,
        action: ChangeAction
    ) {
        FlowManager.getModelNotifierForTable(adapter.table)
            .notifyModelChanged(model, adapter, action)
    }

    /**
     * Notifies listeners of table-level changes from the SQLite-wrapper language.
     */
    override fun <T : Any> notifyTableChanged(
        table: KClass<T>,
        action: ChangeAction
    ) {
        FlowManager.getModelNotifierForTable(table).notifyTableChanged(table, action)
    }

    companion object {

        private val distributor by lazy { NotifyDistributor() }

        @JvmStatic
        fun get(): NotifyDistributor = distributor
    }
}
