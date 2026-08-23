package com.dbflow5.observing.notifications

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Description: Directly notifies about model changes.
 */
object DirectModelNotifier : ModelNotifier {

    private val notifications = MutableSharedFlow<ModelNotification<*>>(
        extraBufferCapacity = 64,
    )

    val notificationFlow: Flow<ModelNotification<*>> = notifications.asSharedFlow()

    override suspend fun <Table : Any> onChange(notification: ModelNotification<Table>) {
        notifications.emit(notification)
    }
}
