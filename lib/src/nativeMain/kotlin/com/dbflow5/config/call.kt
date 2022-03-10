package com.dbflow5.config

/**
 * Just prints to log for now.
 */
actual fun FlowLog.Level.call(
    tag: String,
    message: String?,
    throwable: Throwable?
) {
    val msg = "[${this.name}] $tag: $message"
    print(msg)
    throwable?.printStackTrace()
}
