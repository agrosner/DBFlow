@file:JvmName("TransactionDispatcherJvm")

package com.dbflow5.transaction

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

actual fun defaultTransactionCoroutineDispatcher(): CoroutineDispatcher =
    Executors.newSingleThreadExecutor().asCoroutineDispatcher()
