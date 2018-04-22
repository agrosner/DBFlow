package com.raizlabs.dbflow5.threading

import com.raizlabs.dbflow5.Runnable
import com.raizlabs.dbflow5.config.FlowLog
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException

actual class SameThreadRunnableHandler : RunnableHandler {

    private val executor = Executors.newCachedThreadPool()

    override fun post(runnable: Runnable): Boolean {
        return try {
            executor.submit(runnable)
            true
        } catch (r: RejectedExecutionException) {
            FlowLog.logError(r)
            false
        }
    }
}

actual class MainThreadRunnableHandler : RunnableHandler {

    private val executor = Executors.newSingleThreadExecutor()

    override fun post(runnable: Runnable): Boolean {
        return try {
            executor.submit(runnable)
            true
        } catch (r: RejectedExecutionException) {
            FlowLog.logError(r)
            false
        }
    }
}