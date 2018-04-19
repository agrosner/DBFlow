package com.raizlabs.dbflow5.config

import android.os.Build
import android.util.Log

actual class PlatformLogger {

    actual fun logVerbose(tag: String, message: String?, throwable: Throwable?) {
        Log.v(tag, message, throwable)
    }

    actual fun logDebug(tag: String, message: String?, throwable: Throwable?) {
        Log.d(tag, message, throwable)
    }

    actual fun logInfo(tag: String, message: String?, throwable: Throwable?) {
        Log.i(tag, message, throwable)
    }

    actual fun logWarning(tag: String, message: String?, throwable: Throwable?) {
        Log.w(tag, message, throwable)
    }

    actual fun logError(tag: String, message: String?, throwable: Throwable?) {
        Log.e(tag, message, throwable)
    }

    actual fun logWTF(tag: String, message: String?, throwable: Throwable?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            Log.wtf(tag, message, throwable)
        } else {
            // If on older platform, we will just exaggerate the log message in the error level
            Log.e(tag, "!!!!!!!!*******$message********!!!!!!", throwable)
        }
    }
}
