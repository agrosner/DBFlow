package com.raizlabs.dbflow5.config

import com.raizlabs.dbflow5.JvmOverloads
import com.raizlabs.dbflow5.JvmStatic
import com.raizlabs.dbflow5.config.FlowLog.Level

/**
 * Description: Mirrors [Log] with its own [Level] flag.
 */
object FlowLog {

    const val TAG = "FlowLog"
    private var level = Level.E

    private val actualLogger = PlatformLogger()

    /**
     * Sets the minimum level that we wish to print out log statements with.
     * The default is [Level.E].
     *
     * @param level
     */
    @JvmStatic
    fun setMinimumLoggingLevel(level: Level) {
        FlowLog.level = level
    }

    /**
     * Logs information to the [Log] class. It wraps around the standard implementation.
     *
     * @param level     The log level to use
     * @param tag       The tag of the log
     * @param message   The message to print out
     * @param throwable The optional stack trace to print
     */
    @JvmOverloads
    @JvmStatic
    fun log(level: Level, tag: String = TAG, message: String? = "", throwable: Throwable? = null) {
        if (isEnabled(level)) {
            level.call(tag, message, throwable)
        }
    }

    /**
     * Logs information to the [Log] class. It wraps around the standard implementation.
     *
     * @param level     The log level to use
     * @param tag       The tag of the log
     * @param message   The message to print out
     * @param throwable The optional stack trace to print
     */
    @JvmStatic
    fun log(level: Level, message: String, throwable: Throwable?) = log(level = level, tag = TAG, message = message, throwable = throwable)

    /**
     * Returns true if the logging level is lower than the specified [Level]
     *
     * @return
     */
    @JvmStatic
    fun isEnabled(level: Level) = level.ordinal >= FlowLog.level.ordinal

    /**
     * Logs a [Throwable] as an error.
     *
     * @param throwable The stack trace to print
     */
    @JvmStatic
    fun logError(throwable: Throwable) {
        log(Level.E, throwable = throwable)
    }

    /**
     * Logs a [Throwable] as a warning.
     *
     * @param throwable The stack trace to print
     */
    @JvmStatic
    fun logWarning(throwable: Throwable) {
        log(Level.W, throwable = throwable)
    }

    /**
     * Defines a log level that will execute
     */
    enum class Level {
        V {
            override fun call(tag: String, message: String?, throwable: Throwable?) {
                actualLogger.logVerbose(tag, message, throwable)
            }
        },
        D {
            override fun call(tag: String, message: String?, throwable: Throwable?) {
                actualLogger.logDebug(tag, message, throwable)
            }
        },
        I {
            override fun call(tag: String, message: String?, throwable: Throwable?) {
                actualLogger.logInfo(tag, message, throwable)
            }
        },
        W {
            override fun call(tag: String, message: String?, throwable: Throwable?) {
                actualLogger.logWarning(tag, message, throwable)
            }
        },
        E {
            override fun call(tag: String, message: String?, throwable: Throwable?) {
                actualLogger.logError(tag, message, throwable)
            }
        },
        WTF {
            override fun call(tag: String, message: String?, throwable: Throwable?) {
                actualLogger.logWTF(tag, message, throwable)
            }
        };

        internal abstract fun call(tag: String, message: String?, throwable: Throwable?)
    }

}

expect class PlatformLogger() {

    fun logVerbose(tag: String, message: String?, throwable: Throwable?)

    fun logDebug(tag: String, message: String?, throwable: Throwable?)

    fun logInfo(tag: String, message: String?, throwable: Throwable?)

    fun logWarning(tag: String, message: String?, throwable: Throwable?)

    fun logError(tag: String, message: String?, throwable: Throwable?)

    fun logWTF(tag: String, message: String?, throwable: Throwable?)
}
