package com.dbflow5.database.config

import android.content.Context
import com.dbflow5.config.DBFlowDatabase

/**
 * Used by generated code for database creation.
 */
interface DBCreator<DB : DBFlowDatabase> {

    fun create(context: Context, settingsFn: DBSettings.() -> DBSettings = { this }): DB
}