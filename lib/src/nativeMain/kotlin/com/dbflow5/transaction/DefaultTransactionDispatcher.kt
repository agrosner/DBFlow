package com.dbflow5.transaction

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.newSingleThreadContext

actual fun defaultTransactionCoroutineDispatcher(): CoroutineDispatcher =
    newSingleThreadContext("Custom")
