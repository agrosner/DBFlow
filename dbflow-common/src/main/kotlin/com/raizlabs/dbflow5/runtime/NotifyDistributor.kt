package com.raizlabs.dbflow5.runtime

import com.raizlabs.dbflow5.JvmStatic
import kotlin.reflect.KClass
import com.raizlabs.dbflow5.adapter.ModelAdapter
import com.raizlabs.dbflow5.config.FlowManager
import com.raizlabs.dbflow5.structure.ChangeAction

/**
 * Description: Distributes notifications to the [ModelNotifier].
 */
class NotifyDistributor : ModelNotifier {

    override fun newRegister(): TableNotifierRegister {
        throw RuntimeException("Cannot create a register from the distributor class")
    }

    override fun <T : Any> notifyModelChanged(model: T,
                                              adapter: ModelAdapter<T>,
                                              action: ChangeAction) {
        FlowManager.getModelNotifierForTable(adapter.table)
            .notifyModelChanged(model, adapter, action)
    }

    /**
     * Notifies listeners of table-level changes from the SQLite-wrapper language.
     */
    override fun <T : Any> notifyTableChanged(table: KClass<T>,
                                              action: ChangeAction) {
        FlowManager.getModelNotifierForTable(table).notifyTableChanged(table, action)
    }

    companion object {

        private val distributor by lazy { NotifyDistributor() }

        @JvmStatic
        fun get(): NotifyDistributor = distributor
    }
}
