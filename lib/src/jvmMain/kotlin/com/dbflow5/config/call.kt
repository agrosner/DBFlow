package com.dbflow5.config

import java.util.logging.Level
import java.util.logging.Logger

actual fun FlowLog.Level.call(
    tag: String,
    message: String?,
    throwable: Throwable?
) {
    val logger = Logger.getGlobal()
    val msg = "$tag: $message"
    val level = when (this) {
        FlowLog.Level.V -> Level.ALL
        FlowLog.Level.D -> Level.CONFIG
        FlowLog.Level.I -> Level.INFO
        FlowLog.Level.W -> Level.WARNING
        FlowLog.Level.E -> Level.SEVERE
        FlowLog.Level.WTF -> Level.SEVERE
    }
    logger.log(level, msg, throwable)
}
