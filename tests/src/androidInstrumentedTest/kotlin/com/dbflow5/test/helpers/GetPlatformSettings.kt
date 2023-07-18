package com.dbflow5.test.helpers

import androidx.test.platform.app.InstrumentationRegistry
import com.dbflow5.database.config.DBPlatformSettings

actual fun platformSettings(): DBPlatformSettings = DBPlatformSettings(
    InstrumentationRegistry.getInstrumentation().targetContext
)
