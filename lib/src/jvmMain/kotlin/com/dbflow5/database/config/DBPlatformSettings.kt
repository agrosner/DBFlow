package com.dbflow5.database.config

import com.dbflow5.database.DBFlowDatabase

actual data class DBPlatformSettings(
    /**
     * Used for ability to use Write Ahead Logging
     *
     * What should we consider here?
     */
    actual val isLowRamDevice: Boolean = false,

    ) {
}

/**
 * Creates new [DBFlowDatabase] with settings.
 */
fun <DB : DBFlowDatabase<DB>> DBCreator<DB>.create(
    dbSettings: DBSettings.() -> DBSettings
) = create(DBPlatformSettings(), dbSettings)