package com.dbflow5.runtime

import com.dbflow5.database.DatabaseWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface NotifyDistributor {

    fun <Table : Any> onChange(
        db: DatabaseWrapper,
        notification: ModelNotification<Table>
    )

    companion object {
        private val notifyDistributor: NotifyDistributor = NotifyDistributorImpl()

        operator fun invoke(): NotifyDistributor = notifyDistributor
    }
}

/**
 * Description: Distributes notifications to the [ModelNotifier].
 */
internal data class NotifyDistributorImpl(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : NotifyDistributor {

    private val modelNotifierMap = mutableMapOf<DatabaseWrapper, ModelNotifier>()

    override fun <Table : Any> onChange(
        db: DatabaseWrapper,
        notification: ModelNotification<Table>
    ) {
        scope.launch {
            modelNotifierMap.getOrPut(db) { db.generatedDatabase.modelNotifier }
                .onChange(notification)
        }
    }
}
