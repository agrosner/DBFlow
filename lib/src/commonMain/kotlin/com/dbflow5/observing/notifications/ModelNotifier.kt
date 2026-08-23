package com.dbflow5.observing.notifications

/**
 * Interface for defining how we notify model changes.
 */
interface ModelNotifier {

    suspend fun <Table : Any> onChange(notification: ModelNotification<Table>)

    companion object {
        val Default = DirectModelNotifier
    }
}

fun interface ModelNotifierFactory {
    fun create(): ModelNotifier
}
