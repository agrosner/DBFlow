package com.dbflow5.observing.notifications

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow

/**
 * Description: Directly notifies about model changes.
 */
object DirectModelNotifier : ModelNotifier {

    private val internalNotificationChannel = Channel<ModelNotification<*>>(Channel.UNLIMITED)

    val notificationFlow: Flow<ModelNotification<*>> = internalNotificationChannel.consumeAsFlow()

    override suspend fun <Table : Any> onChange(notification: ModelNotification<Table>) {
        internalNotificationChannel.send(notification)
    }
}
