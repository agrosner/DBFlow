package com.dbflow5.mpp

import com.dbflow5.annotation.opts.InternalDBFlowApi
import com.dbflow5.config.FlowLog

@InternalDBFlowApi
expect interface Closeable {
    fun close()
}

@InternalDBFlowApi
inline fun <C : Closeable, R> C.use(block: (C) -> R): R {
    var exception: Throwable? = null
    try {
        return block(this)
    } catch (e: Throwable) {
        exception = e
        throw e
    } finally {
        when (exception) {
            null -> close()
            else -> try {
                close()
            } catch (closeException: Throwable) {
                // ignored here
                FlowLog.logError(closeException)
            }
        }
    }
}
