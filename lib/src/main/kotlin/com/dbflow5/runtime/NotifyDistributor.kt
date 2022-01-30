package com.dbflow5.runtime

import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.structure.ChangeAction
import kotlin.reflect.KClass

/**
 * Description: Distributes notifications to the [ModelNotifier].
 */
class NotifyDistributor
private constructor(
    override val db: DatabaseWrapper
) : ModelNotifier {

    override fun newRegister(): TableNotifierRegister {
        throw RuntimeException("Cannot create a register from the distributor class")
    }

    override fun <T : Any> notifyModelChanged(
        model: T,
        adapter: ModelAdapter<T>,
        action: ChangeAction
    ) {
        db.associatedDBFlowDatabase.getModelNotifier()
            .notifyModelChanged(model, adapter, action)
    }

    /**
     * Notifies listeners of table-level changes from the SQLite-wrapper language.
     */
    override fun <T : Any> notifyTableChanged(
        table: KClass<T>,
        action: ChangeAction
    ) {
        db.associatedDBFlowDatabase.getModelNotifier().notifyTableChanged(table, action)
    }

    companion object {

        private val distributorMap = mutableMapOf<DatabaseWrapper, NotifyDistributor>()

        operator fun invoke(db: DatabaseWrapper): NotifyDistributor =
            distributorMap.getOrPut(db) {
                NotifyDistributor(db)
            }
    }
}
