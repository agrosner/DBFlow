package com.raizlabs.dbflow5.threading

import android.os.Looper
import android.os.Process.THREAD_PRIORITY_BACKGROUND
import android.os.Process.setThreadPriority
import com.raizlabs.dbflow5.config.FlowLog

actual class ThreadConfigurator {

    actual fun configureForBackground() {
        Looper.prepare()
        setThreadPriority(THREAD_PRIORITY_BACKGROUND)
    }

    actual fun sleep(timeInMillis: Long) {
        try {
            //sleep, and then check for leftovers
            Thread.sleep(timeInMillis)
        } catch (e: InterruptedException) {
            FlowLog.log(FlowLog.Level.I, "DBRequestQueue Batch interrupted to start saving")
        }
    }
}
