package com.raizlabs.android.dbflow.config;

import android.os.Build;
import android.util.Log;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Mirrors {@link android.util.Log} with its own "enabled" flag.
 */
public class FlowLog {

    public static final String TAG = FlowLog.class.getSimpleName();

    /**
     * Defines a log level that will execute
     */
    public enum Level {
        V {
            @Override
            void call(String tag, String message, Throwable throwable) {
                Log.v(tag, message, throwable);
            }
        },
        D {
            @Override
            void call(String tag, String message, Throwable throwable) {
                Log.d(tag, message, throwable);
            }
        },
        I {
            @Override
            void call(String tag, String message, Throwable throwable) {
                Log.i(tag, message, throwable);
            }
        },
        W {
            @Override
            void call(String tag, String message, Throwable throwable) {
                Log.w(tag, message, throwable);
            }
        },
        E {
            @Override
            void call(String tag, String message, Throwable throwable) {
                Log.e(tag, message, throwable);
            }
        },
        WTF {
            @Override
            void call(String tag, String message, Throwable throwable) {
                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.FROYO) {
                    Log.wtf(tag, message, throwable);
                } else {
                    // If on older platform, we will just exaggerate the log message in the error level
                    Log.e(tag, "!!!!!!!!*******" + message + "********!!!!!!", throwable);
                }
            }
        };

        abstract void call(String tag, String message, Throwable throwable);
    }

    private static Level sLevel = Level.E;

    /**
     * Sets the minimum level that we wish to print out log statements with.
     * The default is {@link com.raizlabs.android.dbflow.config.FlowLog.Level#E}.
     * @param level
     */
    public static void setMinimumLoggingLevel(Level level) {
        sLevel = level;
    }

    /**
     * Returns true if the logging level is lower than the specified {@link com.raizlabs.android.dbflow.config.FlowLog.Level}
     * @return
     */
    public static boolean isEnabled(Level level) {
        return level.ordinal()>=sLevel.ordinal();
    }

    /**
     * Logs information to the {@link android.util.Log} class. It wraps around the standard implementation.
     * @param level The log level to use
     * @param tag The tag of the log
     * @param message The message to print out
     * @param throwable The optional stack trace to print
     */
    public static void log(Level level, String tag, String message, Throwable throwable) {
        if (isEnabled(level)) {
            level.call(tag, message, throwable);
        }
    }

    /**
     * Logs information to the {@link android.util.Log} class. It wraps around the standard implementation.
     * It uses the {@link #TAG} for messages
     * @param level The log level to use
     * @param message The message to print out
     * @param throwable The optional stack trace to print
     */
    public static void log(Level level, String message, Throwable throwable) {
        log(level, TAG, message, throwable);
    }

    /**
     * Logs information to the {@link android.util.Log} class. It wraps around the standard implementation.
     * It uses the {@link #TAG} for messages and sends a null throwable.
     * @param level The log level to use
     * @param message The message to print out
     */
    public static void log(Level level, String message) {
        log(level, message, null);
    }

    /**
     * Logs information to the {@link android.util.Log} class. It wraps around the standard implementation.
     * It uses the {@link #TAG} for messages and sends an empty message
     * @param level The log level to use
     * @param throwable The stack trace to print
     */
    public static void log(Level level, Throwable throwable) {
        log(level, TAG, "", throwable);
    }

    /**
     * Logs a {@link java.lang.Throwable} as an error.
     * @param throwable The stack trace to print
     */
    public static void logError(Throwable throwable) {
        log(Level.E, throwable);
    }

}
