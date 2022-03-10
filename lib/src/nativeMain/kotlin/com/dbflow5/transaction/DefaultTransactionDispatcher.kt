package com.dbflow5.transaction

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.newSingleThreadContext
import kotlin.random.Random

actual fun defaultTransactionCoroutineDispatcher(): CoroutineDispatcher =
    newSingleThreadContext(name = Random.nextInt().toString())
