package com.dbflow5.config

import org.slf4j.impl.SimpleLoggerFactory

private val factory = SimpleLoggerFactory()

actual fun FlowLog.Level.call(
    tag: String,
    message: String?,
    throwable: Throwable?
) {
    val logger = factory.getLogger("DBFlow")
    val msg = "$tag: $message"
    when (this) {
        FlowLog.Level.V -> logger.trace(msg, throwable)
        FlowLog.Level.D -> logger.debug(msg, throwable)
        FlowLog.Level.I -> logger.info(msg, throwable)
        FlowLog.Level.W -> logger.warn(msg, throwable)
        FlowLog.Level.E -> logger.error(msg, throwable)
        FlowLog.Level.WTF -> logger.error(msg, throwable)
    }
}
