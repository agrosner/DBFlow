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

fun interface ModelNotifierFactory {
    fun create(db: GeneratedDatabase): ModelNotifier
}
