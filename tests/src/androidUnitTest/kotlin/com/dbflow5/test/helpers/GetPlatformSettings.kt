package com.dbflow5.test.helpers

import com.dbflow5.database.config.DBPlatformSettings
import org.robolectric.RuntimeEnvironment

actual fun platformSettings(): DBPlatformSettings =
    DBPlatformSettings(
        RuntimeEnvironment.getApplication()
    )
