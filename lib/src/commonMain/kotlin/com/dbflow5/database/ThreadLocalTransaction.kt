package com.dbflow5.database

import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

expect class ThreadLocalTransaction() {

    fun acquireTransactionElement(job: Job): CoroutineContext.Element
}
