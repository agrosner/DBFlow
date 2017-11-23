package com.raizlabs.android.dbflow.config

import android.os.Build
import android.util.Log
import com.raizlabs.android.dbflow.config.FlowLog.Level

/**
 * Description: Mirrors [Log] with its own [Level] flag.
 */
object FlowLog {

    val TAG = FlowLog::class.java.simpleName
    private var level = Level.E

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
     * Logs a [java.lang.Throwable] as an error.
     *
     * @param throwable The stack trace to print
     */
    @JvmStatic
    fun logError(throwable: Throwable) {
        log(Level.E, throwable = throwable)
    }

    /**
     * Logs a [java.lang.Throwable] as a warning.
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
                Log.v(tag, message, throwable)
            }
        },
        D {
            override fun call(tag: String, message: String?, throwable: Throwable?) {
                Log.d(tag, message, throwable)
            }
        },
        I {
            override fun call(tag: String, message: String?, throwable: Throwable?) {
                Log.i(tag, message, throwable)
            }
        },
        W {
            override fun call(tag: String, message: String?, throwable: Throwable?) {
                Log.w(tag, message, throwable)
            }
        },
        E {
            override fun call(tag: String, message: String?, throwable: Throwable?) {
                Log.e(tag, message, throwable)
            }
        },
        WTF {
            override fun call(tag: String, message: String?, throwable: Throwable?) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                    Log.wtf(tag, message, throwable)
                } else {
                    // If on older platform, we will just exaggerate the log message in the error level
                    Log.e(tag, "!!!!!!!!*******$message********!!!!!!", throwable)
                }
            }
        };

        internal abstract fun call(tag: String, message: String?, throwable: Throwable?)
    }

}
/**
 * Logs information to the [Log] class. It wraps around the standard implementation.
 * It uses the [.TAG] for messages and sends a null throwable.
 *
 * @param level   The log level to use
 * @param message The message to print out
 */
