package com.dbflow5.database.config

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext

actual class DBPlatformSettings(
    /**
     * Used for ability to use Write Ahead Logging
     */
    actual val isLowRamDevice: Boolean = false,
    actual val transactionCoroutineDispatcher: CoroutineDispatcher = newSingleThreadContext("TransactionDispatcher"),
    actual val callbackDispatcher: CoroutineDispatcher = Dispatchers.Main
)
