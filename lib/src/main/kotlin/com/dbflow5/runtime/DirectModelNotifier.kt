package com.dbflow5.runtime

import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.config.DatabaseConfig
import com.dbflow5.database.DatabaseWrapper
import kotlin.reflect.KClass

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

    private val listenerMap = mutableMapOf<KClass<*>, MutableSet<ModelNotificationListener<*>>>()

    override fun <Table : Any> onChange(notification: ModelNotification<Table>) {
        synchronized(listenerMap) {
            listenerMap[notification.table]?.forEach { listener ->
                listener
                    .cast<Table>()
                    .onChange(notification)
            }
        }
    }

    inline fun <reified Table : Any> addListener(
        modelNotificationListener: ModelNotificationListener<Table>
    ) = addListener(Table::class, modelNotificationListener)

    fun <Table : Any> addListener(
        table: KClass<Table>,
        modelNotificationListener: ModelNotificationListener<Table>
    ) {
        listenerMap.getOrPut(table) {
            linkedSetOf()
        }.add(modelNotificationListener)
    }

    inline fun <reified Table : Any> removeListener(
        modelNotificationListener: ModelNotificationListener<Table>
    ) = removeListener(Table::class, modelNotificationListener)

    fun <Table : Any> removeListener(
        table: KClass<Table>,
        modelNotificationListener: ModelNotificationListener<Table>,
    ) {
        listenerMap[table]?.remove(modelNotificationListener)
    }

    /**
     * Clears all listeners.
     */
    fun clearListeners() = listenerMap.clear()

    companion object {
        private val notifierMap = mutableMapOf<DatabaseWrapper, DirectModelNotifier>()

        fun get(db: DBFlowDatabase): DirectModelNotifier =
            notifierMap.getOrPut(db) {
                DirectModelNotifier(db)
            }
    }
}
