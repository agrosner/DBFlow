package com.dbflow5.test.helpers

import com.dbflow5.database.config.DBPlatformSettings
import kotlinx.coroutines.test.StandardTestDispatcher

actual fun platformSettings(): DBPlatformSettings = DBPlatformSettings(
    callbackDispatcher = StandardTestDispatcher()
)
