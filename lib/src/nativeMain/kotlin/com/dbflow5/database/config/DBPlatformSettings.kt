package com.dbflow5.database.config

/**
 * Description:
 */
actual class DBPlatformSettings(
    /**
     * Used for ability to use Write Ahead Logging
     */
    actual val isLowRamDevice: Boolean = false
)
