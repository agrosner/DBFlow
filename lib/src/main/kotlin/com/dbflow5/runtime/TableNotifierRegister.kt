package com.dbflow5.runtime

import com.dbflow5.structure.ChangeAction

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

inline fun TableNotifierRegister.setListener(crossinline listener: (Class<*>?, ChangeAction) -> Unit) =
        setListener(object : OnTableChangedListener {
            override fun onTableChanged(table: Class<*>?, action: ChangeAction) = listener(table, action)
        })

