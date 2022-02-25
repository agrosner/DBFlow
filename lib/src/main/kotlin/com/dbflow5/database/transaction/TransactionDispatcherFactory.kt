package com.dbflow5.database.transaction

import com.dbflow5.transaction.TransactionDispatcher

fun interface TransactionDispatcherFactory {
    fun create(): TransactionDispatcher
}