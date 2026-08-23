package com.dbflow5.config

import com.dbflow5.database.config.DBPlatformSettings

enum class JournalMode {
    Automatic,
    Truncate,
    WriteAheadLogging;

    fun adjustIfAutomatic(settings: DBPlatformSettings): JournalMode = when (this) {
        Automatic -> this
        else -> {
            // check if low ram device
            if (!settings.isLowRamDevice) {
                WriteAheadLogging
            } else {
                Truncate
            }
        }
    }
}
