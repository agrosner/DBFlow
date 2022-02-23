package com.dbflow5.runtime

import com.dbflow5.config.GeneratedDatabase
import com.dbflow5.database.DatabaseWrapper

/**
 * Interface for defining how we notify model changes.
 */
interface ModelNotifier {

    val db: DatabaseWrapper

    suspend fun <Table : Any> onChange(notification: ModelNotification<Table>)
}

/**
 * Creates a default ModelNotifier.
 */
@Suppress("FunctionName")
fun ModelNotifier(db: GeneratedDatabase) = DirectModelNotifier(db)

fun interface ModelNotificationListener<Table : Any> {
    fun onChange(notification: ModelNotification<Table>)
}

/**
 * Casts the listener out of nothing into something..
 */
@Suppress("UNCHECKED_CAST")
internal fun <Table : Any> ModelNotificationListener<*>.cast() =
    this as ModelNotificationListener<Table>

fun interface ModelNotifierFactory {
    fun create(db: GeneratedDatabase): ModelNotifier
}