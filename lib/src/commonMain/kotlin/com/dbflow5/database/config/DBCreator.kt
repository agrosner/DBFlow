package com.dbflow5.database.config

import com.dbflow5.database.DBFlowDatabase

/**
 * Used by generated code for database creation.
 */
interface DBCreator<DB : DBFlowDatabase<DB>> {

    fun create(
        platformSettings: DBPlatformSettings,
        settingsFn: DBSettings.() -> DBSettings = { this }
    ): DB
}
