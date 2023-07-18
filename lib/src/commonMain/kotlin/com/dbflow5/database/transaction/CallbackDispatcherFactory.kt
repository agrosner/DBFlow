package com.dbflow5.database.transaction

import com.dbflow5.transaction.CallbackDispatcher

fun interface CallbackDispatcherFactory {

    fun create(): CallbackDispatcher
}