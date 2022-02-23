package com.dbflow5.runtime

import com.dbflow5.config.DatabaseConfig
import com.dbflow5.config.GeneratedDatabase
import com.dbflow5.database.DatabaseWrapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNotNull

/**
 * Description: Directly notifies about model changes. Users should use [.get] to use the shared
 * instance in [DatabaseConfig.Builder]
 */
class DirectModelNotifier(
    override val db: GeneratedDatabase,
) : ModelNotifier {

    private val internalNotificationFlow = MutableSharedFlow<ModelNotification<*>>(1)

    val notificationFlow: Flow<ModelNotification<*>>
        get() = internalNotificationFlow.filterNotNull()

    override suspend fun <Table : Any> onChange(notification: ModelNotification<Table>) {
        internalNotificationFlow.emit(notification)
    }

    companion object {
        private val notifierMap = mutableMapOf<DatabaseWrapper, DirectModelNotifier>()

        fun get(db: GeneratedDatabase): DirectModelNotifier =
            notifierMap.getOrPut(db) {
                DirectModelNotifier(db)
            }
    }
}
