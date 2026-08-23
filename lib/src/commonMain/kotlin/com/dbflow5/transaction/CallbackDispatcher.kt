package com.dbflow5.transaction

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Description: Provides a callback post transaction.
 */
interface CallbackDispatcher {

    /**
     * Dispatcher to callback after transaction completes.
     */
    val dispatcher: CoroutineDispatcher
}


fun CallbackDispatcher(
    dispatcher: CoroutineDispatcher = Dispatchers.Main
): CallbackDispatcher = object : CallbackDispatcher {
    override val dispatcher: CoroutineDispatcher = dispatcher
}
