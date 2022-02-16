package com.dbflow5.runtime

import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.config.DatabaseConfig
import com.dbflow5.database.DatabaseWrapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

/**
 * Description: Directly notifies about model changes. Users should use [.get] to use the shared
 * instance in [DatabaseConfig.Builder]
 */
class DirectModelNotifier
/**
 * Private constructor. Use shared [.get] to ensure singular instance.
 */
private constructor(
    override val db: DBFlowDatabase,
) : ModelNotifier {

    private val internalNotificationFlow = MutableSharedFlow<ModelNotification<*>>(1)

    val notificationFlow: Flow<ModelNotification<*>>
        get() = internalNotificationFlow.filterNotNull()

    override suspend fun <Table : Any> onChange(notification: ModelNotification<Table>) {
        internalNotificationFlow.emit(notification)
    }

    companion object {
        private val notifierMap = mutableMapOf<DatabaseWrapper, DirectModelNotifier>()

        fun get(db: DBFlowDatabase): DirectModelNotifier =
            notifierMap.getOrPut(db) {
                DirectModelNotifier(db)
            }
    }
}
