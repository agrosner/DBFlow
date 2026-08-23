package com.dbflow5.database

import kotlinx.coroutines.Job
import kotlinx.coroutines.asContextElement
import kotlin.coroutines.CoroutineContext

actual class ThreadLocalTransaction {
    private val transactionId = ThreadLocal<Int>()

    actual fun acquireTransactionElement(job: Job): CoroutineContext.Element =
        transactionId.asContextElement(System.identityHashCode(job))
}
