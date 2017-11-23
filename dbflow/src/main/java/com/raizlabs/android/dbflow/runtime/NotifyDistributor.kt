package com.raizlabs.android.dbflow.runtime

import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.structure.ModelAdapter

/**
 * Description: Distributes notifications to the [ModelNotifier].
 */
class NotifyDistributor : ModelNotifier {

    override fun newRegister(): TableNotifierRegister {
        throw RuntimeException("Cannot create a register from the distributor class")
    }

    override fun <T : Any> notifyModelChanged(model: T,
                                              adapter: ModelAdapter<T>,
                                              action: BaseModel.Action) {
        FlowManager.getModelNotifierForTable(adapter.modelClass)
                .notifyModelChanged(model, adapter, action)
    }

    /**
     * Notifies listeners of table-level changes from the SQLite-wrapper language.
     */
    override fun <T : Any> notifyTableChanged(table: Class<T>,
                                              action: BaseModel.Action) {
        FlowManager.getModelNotifierForTable(table).notifyTableChanged(table, action)
    }

    companion object {

        private val distributor by lazy { NotifyDistributor() }

        @JvmStatic
        fun get(): NotifyDistributor = distributor
    }
}
