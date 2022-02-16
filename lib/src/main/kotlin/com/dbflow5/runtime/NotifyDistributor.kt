package com.dbflow5.runtime

import com.dbflow5.database.DatabaseWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Description: Distributes notifications to the [ModelNotifier].
 */
class NotifyDistributor
private constructor(
    private val modelNotifier: ModelNotifier,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main),
) {

    fun <Table : Any> onChange(notification: ModelNotification<Table>) {
        scope.launch { modelNotifier.onChange(notification) }
    }

    companion object {

        private val distributorMap = mutableMapOf<DatabaseWrapper, NotifyDistributor>()

        operator fun invoke(db: DatabaseWrapper): NotifyDistributor =
            distributorMap.getOrPut(db) {
                NotifyDistributor(db.associatedDBFlowDatabase.getModelNotifier())
            }
    }
}
