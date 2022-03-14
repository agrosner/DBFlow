package com.dbflow5.observing.notifications

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Description: Directly notifies about model changes. Users should use [.get] to use the shared
 */
class DirectModelNotifier(
    override val notificationScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : ModelNotifier {

    private val internalNotificationFlow = MutableSharedFlow<ModelNotification<*>>(1)

    val notificationFlow: Flow<ModelNotification<*>> = internalNotificationFlow

    override suspend fun <Table : Any> onChange(notification: ModelNotification<Table>) {
        internalNotificationFlow.emit(notification)
    }
}
