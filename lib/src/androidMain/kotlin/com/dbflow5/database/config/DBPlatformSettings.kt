package com.dbflow5.database.config

import android.app.ActivityManager
import android.content.Context
import com.dbflow5.config.DBFlowDatabase

actual data class DBPlatformSettings(
    val context: Context
) {
    /**
     * Used for ability to use Write Ahead Logging
     */
    actual val isLowRamDevice: Boolean
        get() = (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?)
            ?.isLowRamDevice == false
}

/**
 * Creates the DB with [context] parameter.
 */
fun <DB : DBFlowDatabase> DBCreator<DB>.create(
    context: Context,
    dbSettings: DBSettings.() -> DBSettings
) = create(DBPlatformSettings(context), dbSettings)
