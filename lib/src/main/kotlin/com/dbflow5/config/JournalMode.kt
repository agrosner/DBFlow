package com.dbflow5.config

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi

enum class JournalMode {
    Automatic,
    Truncate,

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
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
