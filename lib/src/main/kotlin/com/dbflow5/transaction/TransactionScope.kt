package com.dbflow5.transaction

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

fun createTransactionScope() = CoroutineScope(
    Dispatchers.IO
)