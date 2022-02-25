package com.dbflow5.config

import android.app.ActivityManager
import android.content.Context

enum class JournalMode {
    Automatic,
    Truncate,
    WriteAheadLogging;

    fun adjustIfAutomatic(context: Context): JournalMode = when (this) {
        Automatic -> this
        else -> {
            // check if low ram device
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
            if (manager?.isLowRamDevice == false) {
                WriteAheadLogging
            } else {
                Truncate
            }
        }
    }
}
