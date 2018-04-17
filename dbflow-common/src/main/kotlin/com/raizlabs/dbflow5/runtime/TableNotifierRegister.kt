package com.raizlabs.dbflow5.runtime

import kotlin.reflect.KClass

/**
 * Description: Defines how [ModelNotifier] registers listeners. Abstracts that away.
 */
interface TableNotifierRegister {

    val isSubscribed: Boolean

    fun <T: Any> register(tClass: KClass<T>)

    fun <T: Any> unregister(tClass: KClass<T>)

    fun unregisterAll()

    fun setListener(listener: OnTableChangedListener?)
}
