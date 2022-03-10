package com.dbflow5.database

import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

actual class ThreadLocalTransaction {

    actual fun acquireTransactionElement(job: Job): CoroutineContext.Element {
        return ThreadLocalContextElement(value = job.hashCode())
    }
}

private data class ThreadLocalKey<V>(private val value: V) :
    CoroutineContext.Key<ThreadLocalContextElement<*>>

private class ThreadLocalContextElement<V>(
    value: V,
) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> = ThreadLocalKey(value)
}