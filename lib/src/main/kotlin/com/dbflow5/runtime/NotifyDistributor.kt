package com.dbflow5.runtime

import com.dbflow5.database.DatabaseWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface NotifyDistributor {

    fun <Table : Any> onChange(notification: ModelNotification<Table>)

    companion object {

        private val distributorMap = mutableMapOf<DatabaseWrapper, NotifyDistributor>()

        operator fun invoke(db: DatabaseWrapper): NotifyDistributor =
            distributorMap.getOrPut(db) {
                NotifyDistributorImpl(db.generatedDatabase.modelNotifier)
            }
    }
}

/**
 * Description: Distributes notifications to the [ModelNotifier].
 */
internal data class NotifyDistributorImpl(
    private val modelNotifier: ModelNotifier,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : NotifyDistributor {

    override fun <Table : Any> onChange(notification: ModelNotification<Table>) {
        scope.launch { modelNotifier.onChange(notification) }
    }
}
