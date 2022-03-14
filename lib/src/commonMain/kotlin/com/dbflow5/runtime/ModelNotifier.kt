package com.dbflow5.runtime

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Interface for defining how we notify model changes.
 */
interface ModelNotifier {

    val notificationScope: CoroutineScope

    fun <Table : Any> enqueueChange(notification: ModelNotification<Table>) {
        notificationScope.launch {
            onChange(notification)
        }
    }

    suspend fun <Table : Any> onChange(notification: ModelNotification<Table>)
}

/**
 * Creates a default ModelNotifier.
 */
@Suppress("FunctionName")
fun ModelNotifier(notificationScope: CoroutineScope = CoroutineScope(Dispatchers.Main)) =
    DirectModelNotifier(notificationScope = notificationScope)

fun interface ModelNotifierFactory {
    fun create(): ModelNotifier
}
