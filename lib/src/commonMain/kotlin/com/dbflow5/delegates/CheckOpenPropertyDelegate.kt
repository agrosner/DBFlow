package com.dbflow5.delegates

import com.dbflow5.annotation.opts.InternalDBFlowApi
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface CheckOpen {
    val isOpen: Boolean
}

@InternalDBFlowApi
internal class CheckOpenPropertyDelegate<T : CheckOpen>(
    private val onOpen: (T) -> Unit,
    private val factory: () -> T
) : ReadOnlyProperty<Any?, T> {

    private var value: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        var localValue = value
        if (localValue == null || !localValue.isOpen) {
            localValue = factory()
        }
        return localValue.also {
            value = it
            val isOpen = it.isOpen
            if (isOpen) {
                onOpen(it)
            }
        }
    }
}

@InternalDBFlowApi
internal fun <T : CheckOpen> checkOpen(factory: () -> T): ReadOnlyProperty<Any?, T> =
    CheckOpenPropertyDelegate(onOpen = {}, factory)
