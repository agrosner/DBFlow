package com.dbflow5.config

import android.util.Log

actual fun FlowLog.Level.call(
    tag: String,
    message: String?,
    throwable: Throwable?
): Unit = when (this) {
    FlowLog.Level.V -> Log.v(tag, message, throwable)
    FlowLog.Level.D -> Log.d(tag, message, throwable)
    FlowLog.Level.I -> Log.i(tag, message, throwable)
    FlowLog.Level.W -> Log.w(tag, message, throwable)
    FlowLog.Level.E -> Log.e(tag, message, throwable)
    FlowLog.Level.WTF -> Log.wtf(tag, message, throwable)
}.run { }
