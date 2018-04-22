package com.raizlabs.dbflow5.config

import java.util.logging.Level
import java.util.logging.Logger

actual class PlatformLogger actual constructor() {

    private val logger = Logger.getGlobal()

    actual fun logVerbose(tag: String, message: String?, throwable: Throwable?) {
        logMessage(Level.FINEST, throwable, message)
    }

    actual fun logDebug(tag: String, message: String?, throwable: Throwable?) {
        logMessage(Level.FINE, throwable, message)
    }

    actual fun logInfo(tag: String, message: String?, throwable: Throwable?) {
        logMessage(Level.INFO, throwable, message)
    }

    actual fun logWarning(tag: String, message: String?, throwable: Throwable?) {
        logMessage(Level.WARNING, throwable, message)
    }

    actual fun logError(tag: String, message: String?, throwable: Throwable?) {
        logMessage(Level.SEVERE, throwable, message)
    }

    actual fun logWTF(tag: String, message: String?, throwable: Throwable?) {
        logMessage(Level.SEVERE, throwable, message)
    }

    private fun logMessage(level: Level, throwable: Throwable?, message: String?) {
        if (throwable != null) {
            throwable.printStackTrace()
        } else {
            logger.log(level, message)
        }
    }

}