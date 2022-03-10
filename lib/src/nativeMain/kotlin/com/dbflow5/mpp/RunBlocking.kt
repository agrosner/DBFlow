package com.dbflow5.mpp

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext


actual fun <T> runBlocking(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> T
): T =
    kotlinx.coroutines.runBlocking(context, block)
