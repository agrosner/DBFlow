package com.dbflow5.config

import co.touchlab.stately.isolate.IsolateState
import com.dbflow5.config.FlowLog.Level
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

private data class MutableLevel(
    var level: Level = Level.E,
)

/**
 * Main logging mechanis in the library.
 */
object FlowLog {

    val TAG = "FlowLog"
    private val level = IsolateState { MutableLevel() }

    /**
     * Sets the minimum level that we wish to print out log statements with.
     * The default is [Level.E].
     *
     * @param level
     */
    @JvmStatic
    fun setMinimumLoggingLevel(level: Level) {
        FlowLog.level.access { it.level = level }
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
    fun log(level: Level, tag: String = TAG, message: Any? = "", throwable: Throwable? = null) {
        if (isEnabled(level)) {
            level.call(tag, message?.toString(), throwable)
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
    fun log(level: Level, message: Any, throwable: Throwable?) =
        log(level = level, tag = TAG, message = message, throwable = throwable)

    /**
     * Returns true if the logging level is lower than the specified [Level]
     *
     * @return
     */
    @JvmStatic
    fun isEnabled(level: Level) = level.ordinal >= FlowLog.level.access { it.level.ordinal }

    /**
     * Logs a [Throwable] as an error.
     *
     * @param throwable The stack trace to print
     */
    @JvmStatic
    fun logError(throwable: Throwable, message: Any? = null) {
        log(Level.E, throwable = throwable, message = message)
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
        V,
        D,
        I,
        W,
        E,
        WTF;
    }
}

expect fun Level.call(tag: String, message: String? = null, throwable: Throwable? = null)
