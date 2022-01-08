package com.dbflow5.runtime

import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.config.DatabaseConfig
import com.dbflow5.structure.ChangeAction
import kotlin.reflect.KClass

/**
 * Description: Directly notifies about model changes. Users should use [.get] to use the shared
 * instance in [DatabaseConfig.Builder]
 */
class DirectModelNotifier
/**
 * Private constructor. Use shared [.get] to ensure singular instance.
 */
private constructor() : ModelNotifier {

    private val modelChangedListenerMap =
        linkedMapOf<KClass<*>, MutableSet<OnModelStateChangedListener<*>>>()

    private val tableChangedListenerMap =
        linkedMapOf<KClass<*>, MutableSet<OnTableChangedListener>>()

    interface OnModelStateChangedListener<in T> {
        fun onModelChanged(model: T, action: ChangeAction)
    }

    interface ModelChangedListener<in T> : OnModelStateChangedListener<T>, OnTableChangedListener

    init {
        if (instanceCount > 0) {
            throw IllegalStateException("Cannot instantiate more than one DirectNotifier. Use DirectNotifier.get()")
        }
        instanceCount++
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> notifyModelChanged(
        model: T, adapter: ModelAdapter<T>,
        action: ChangeAction
    ) {
        modelChangedListenerMap[adapter.table]
            ?.forEach { listener ->
                (listener as OnModelStateChangedListener<T>).onModelChanged(model, action)
            }
        tableChangedListenerMap[adapter.table]
            ?.forEach { listener -> listener.onTableChanged(adapter.table, action) }
    }

    override fun <T : Any> notifyTableChanged(table: KClass<T>, action: ChangeAction) {
        tableChangedListenerMap[table]?.forEach { listener ->
            listener.onTableChanged(
                table,
                action
            )
        }
    }

    override fun newRegister(): TableNotifierRegister = DirectTableNotifierRegister(this)

    fun <T : Any> registerForModelChanges(
        table: KClass<T>,
        listener: ModelChangedListener<T>
    ) {
        registerForModelStateChanges(table, listener)
        registerForTableChanges(table, listener)
    }

    fun <T : Any> registerForModelStateChanges(
        table: KClass<T>,
        listener: OnModelStateChangedListener<T>
    ) {
        var listeners = modelChangedListenerMap[table]
        if (listeners == null) {
            listeners = linkedSetOf()
            modelChangedListenerMap[table] = listeners
        }
        listeners.add(listener)
    }

    fun <T : Any> registerForTableChanges(
        table: KClass<T>,
        listener: OnTableChangedListener
    ) {
        var listeners = tableChangedListenerMap[table]
        if (listeners == null) {
            listeners = linkedSetOf()
            tableChangedListenerMap[table] = listeners
        }
        listeners.add(listener)
    }

    fun <T : Any> unregisterForModelChanges(
        table: KClass<T>,
        listener: ModelChangedListener<T>
    ) {
        unregisterForModelStateChanges(table, listener)
        unregisterForTableChanges(table, listener)
    }


    fun <T : Any> unregisterForModelStateChanges(
        table: KClass<T>,
        listener: OnModelStateChangedListener<T>
    ) {
        val listeners = modelChangedListenerMap[table]
        listeners?.remove(listener)
    }

    fun <T : Any> unregisterForTableChanges(
        table: KClass<T>,
        listener: OnTableChangedListener
    ) {
        val listeners = tableChangedListenerMap[table]
        listeners?.remove(listener)
    }

    /**
     * Clears all listeners.
     */
    fun clearListeners() = tableChangedListenerMap.clear()

    private class DirectTableNotifierRegister(private val directModelNotifier: DirectModelNotifier) :
        TableNotifierRegister {
        private val registeredTables = arrayListOf<KClass<*>>()

        private var modelChangedListener: OnTableChangedListener? = null

        private val internalChangeListener = object : OnTableChangedListener {
            override fun onTableChanged(table: KClass<*>?, action: ChangeAction) {
                modelChangedListener?.onTableChanged(table, action)
            }
        }

        override fun <T: Any> register(tClass: KClass<T>) {
            registeredTables.add(tClass)
            directModelNotifier.registerForTableChanges(tClass, internalChangeListener)
        }

        override fun <T: Any> unregister(tClass: KClass<T>) {
            registeredTables.remove(tClass)
            directModelNotifier.unregisterForTableChanges(tClass, internalChangeListener)
        }

        override fun unregisterAll() {
            registeredTables.forEach { table ->
                directModelNotifier.unregisterForTableChanges(table, internalChangeListener)
            }
            this.modelChangedListener = null
        }

        override fun setListener(listener: OnTableChangedListener?) {
            this.modelChangedListener = listener
        }

        override val isSubscribed: Boolean
            get() = !registeredTables.isEmpty()
    }

    companion object {

        internal var instanceCount = 0

        private val notifier: DirectModelNotifier by lazy {
            DirectModelNotifier()
        }

        @JvmStatic
        fun get(): DirectModelNotifier = notifier

        operator fun invoke() = get()
    }

}
