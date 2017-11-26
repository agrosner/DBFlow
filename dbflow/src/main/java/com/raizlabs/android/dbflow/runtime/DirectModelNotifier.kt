package com.raizlabs.android.dbflow.runtime

import com.raizlabs.android.dbflow.config.DatabaseConfig
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.structure.ModelAdapter
import java.util.*

/**
 * Description: Directly notifies about model changes. Users should use [.get] to use the shared
 * instance in [DatabaseConfig.Builder]
 */
class DirectModelNotifier
/**
 * Private constructor. Use shared [.get] to ensure singular instance.
 */
private constructor() : ModelNotifier {

    private val modelChangedListenerMap = linkedMapOf<Class<*>, MutableSet<OnModelStateChangedListener<*>>>()

    private val tableChangedListenerMap = linkedMapOf<Class<*>, MutableSet<OnTableChangedListener>>()


    private val singleRegister = DirectTableNotifierRegister()

    interface OnModelStateChangedListener<in T> {

        fun onModelChanged(model: T, action: BaseModel.Action)

    }

    interface ModelChangedListener<in T> : OnModelStateChangedListener<T>, OnTableChangedListener

    init {
        if (instanceCount > 0) {
            throw IllegalStateException("Cannot instantiate more than one DirectNotifier. Use DirectNotifier.get()")
        }
        instanceCount++
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> notifyModelChanged(model: T, adapter: ModelAdapter<T>,
                                              action: BaseModel.Action) {
        modelChangedListenerMap[adapter.modelClass]?.forEach { listener ->
            (listener as OnModelStateChangedListener<T>).onModelChanged(model, action)
        }
    }

    override fun <T : Any> notifyTableChanged(table: Class<T>, action: BaseModel.Action) {
        tableChangedListenerMap[table]?.forEach { listener -> listener.onTableChanged(table, action) }
    }

    override fun newRegister(): TableNotifierRegister {
        return singleRegister
    }

    fun <T : Any> registerForModelChanges(table: Class<T>,
                                          listener: ModelChangedListener<T>) {
        registerForModelStateChanges(table, listener)
        registerForTableChanges(table, listener)
    }

    fun <T : Any> registerForModelStateChanges(table: Class<T>,
                                               listener: OnModelStateChangedListener<T>) {
        var listeners = modelChangedListenerMap[table]
        if (listeners == null) {
            listeners = linkedSetOf()
            modelChangedListenerMap.put(table, listeners)
        }
        listeners.add(listener)
    }

    fun <T> registerForTableChanges(table: Class<T>,
                                    listener: OnTableChangedListener) {
        var listeners = tableChangedListenerMap[table]
        if (listeners == null) {
            listeners = linkedSetOf()
            tableChangedListenerMap.put(table, listeners)
        }
        listeners.add(listener)
    }

    fun <T : Any> unregisterForModelChanges(table: Class<T>,
                                            listener: ModelChangedListener<T>) {
        unregisterForModelStateChanges(table, listener)
        unregisterForTableChanges(table, listener)
    }


    fun <T : Any> unregisterForModelStateChanges(table: Class<T>,
                                                 listener: OnModelStateChangedListener<T>) {
        val listeners = modelChangedListenerMap[table]
        listeners?.remove(listener)
    }

    fun <T> unregisterForTableChanges(table: Class<T>,
                                      listener: OnTableChangedListener) {
        val listeners = tableChangedListenerMap[table]
        listeners?.remove(listener)
    }

    private inner class DirectTableNotifierRegister : TableNotifierRegister {
        private val registeredTables = ArrayList<Class<*>>()

        private var modelChangedListener: OnTableChangedListener? = null

        private val internalChangeListener = object : OnTableChangedListener {
            override fun onTableChanged(table: Class<*>?, action: BaseModel.Action) {
                modelChangedListener?.onTableChanged(table, action)
            }
        }

        override fun <T> register(tClass: Class<T>) {
            registeredTables.add(tClass)
            registerForTableChanges(tClass, internalChangeListener)
        }

        override fun <T> unregister(tClass: Class<T>) {
            registeredTables.remove(tClass)
            unregisterForTableChanges(tClass, internalChangeListener)
        }

        override fun unregisterAll() {
            registeredTables.forEach { table -> unregisterForTableChanges(table, internalChangeListener) }
            this.modelChangedListener = null
        }

        override fun setListener(modelChangedListener: OnTableChangedListener?) {
            this.modelChangedListener = modelChangedListener
        }

        override val isSubscribed: Boolean
            get() = !registeredTables.isEmpty()
    }

    companion object {

        internal var instanceCount = 0;

        private val notifier: DirectModelNotifier by lazy {
            DirectModelNotifier()
        }

        @JvmStatic
        fun get(): DirectModelNotifier = notifier

        operator fun invoke() = get()
    }

}
