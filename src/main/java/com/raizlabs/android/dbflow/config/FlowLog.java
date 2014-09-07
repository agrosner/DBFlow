package com.raizlabs.android.dbflow.config;

import android.util.Log;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Mirrors {@link android.util.Log} with its own "enabled" flag.
 */
public class FlowLog {

    private static boolean sEnabled = false;

    public static void setLoggingEnabled(boolean enabled) {
        sEnabled = enabled;
    }

    public static boolean isEnabled() {
        return sEnabled;
    }
    /**
     * Send a {@link android.util.Log#VERBOSE} log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void v(String tag, String msg) {
        if(sEnabled) {
            Log.v(tag, msg);
        }
    }

    /**
     * Send a {@link android.util.Log#VERBOSE} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static void v(String tag, String msg, Throwable tr) {
        if(sEnabled) {
            Log.v(tag, msg, tr);
        }
    }

    /**
     * Send a {@link android.util.Log#DEBUG} log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void d(String tag, String msg) {
        if(sEnabled) {
            Log.d(tag, msg);
        }
    }

    /**
     * Send a {@link android.util.Log#DEBUG} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static void d(String tag, String msg, Throwable tr) {
        if(sEnabled) {
            Log.d(tag, msg, tr);
        }
    }

    /**
     * Send an {@link android.util.Log#INFO} log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void i(String tag, String msg) {
        if(sEnabled) {
            Log.i(tag, msg);
        }
    }

    /**
     * Send a {@link android.util.Log#INFO} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static void i(String tag, String msg, Throwable tr) {
        if(sEnabled) {
            Log.i(tag, msg, tr);
        }
    }

    /**
     * Send a {@link android.util.Log#WARN} log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void w(String tag, String msg) {
        if(sEnabled) {
            Log.w(tag, msg);
        }
    }

    /**
     * Send a {@link android.util.Log#WARN} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static void w(String tag, String msg, Throwable tr) {
        if(sEnabled) {
            Log.w(tag, msg, tr);
        }
    }

    /**
     * Send a {@link android.util.Log#WARN} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param tr An exception to log
     */
    public static void w(String tag, Throwable tr) {
        if(sEnabled) {
            Log.w(tag, tr);
        }
    }

    /**
     * Send an {@link android.util.Log#ERROR} log message.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    public static void e(String tag, String msg) {
        if(sEnabled) {
            Log.e(tag, msg);
        }
    }

    /**
     * Send a {@link android.util.Log#ERROR} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static void e(String tag, String msg, Throwable tr) {
        if(sEnabled) {
            Log.e(tag, msg, tr);
        }
    }

    /**
     * What a Terrible Failure: Report a condition that should never happen.
     * The error will always be logged at level ASSERT with the call stack.
     * Depending on system configuration, a report may be added to the
     * {@link android.os.DropBoxManager} and/or the process may be terminated
     * immediately with an error dialog.
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    public static void wtf(String tag, String msg) {
        if(sEnabled) {
            Log.wtf(tag, msg);
        }
    }

    /**
     * What a Terrible Failure: Report an exception that should never happen.
     * Similar to {@link #wtf(String, String)}, with an exception to log.
     * @param tag Used to identify the source of a log message.
     * @param tr An exception to log.
     */
    public static void wtf(String tag, Throwable tr) {
        if(sEnabled) {
            Log.wtf(tag, tr);
        }
    }

    /**
     * What a Terrible Failure: Report an exception that should never happen.
     * Similar to {@link #wtf(String, Throwable)}, with a message as well.
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     * @param tr An exception to log.  May be null.
     */
    public static void wtf(String tag, String msg, Throwable tr) {
        if(sEnabled) {
            Log.wtf(tag, msg, tr);
        }
    }
}
