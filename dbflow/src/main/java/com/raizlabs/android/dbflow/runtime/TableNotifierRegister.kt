package com.raizlabs.android.dbflow.runtime

/**
 * Description: Defines how [ModelNotifier] registers listeners. Abstracts that away.
 */
interface TableNotifierRegister {

    val isSubscribed: Boolean

    fun <T> register(tClass: Class<T>)

    fun <T> unregister(tClass: Class<T>)

    fun unregisterAll()

    fun setListener(listener: OnTableChangedListener?)
}
