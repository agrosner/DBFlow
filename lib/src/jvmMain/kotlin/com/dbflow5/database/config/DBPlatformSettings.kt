package com.dbflow5.database.config

import com.dbflow5.database.DBFlowDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

actual data class DBPlatformSettings(
    /**
     * Used for ability to use Write Ahead Logging
     *
     * What should we consider here?
     */
    actual val isLowRamDevice: Boolean = false,
    actual val transactionCoroutineDispatcher: CoroutineDispatcher =
        Executors.newSingleThreadExecutor().asCoroutineDispatcher(),
    actual val callbackDispatcher: CoroutineDispatcher = Dispatchers.Main
)

/**
 * Creates new [DBFlowDatabase] with settings.
 */
fun <DB : DBFlowDatabase<DB>> DBCreator<DB>.create(
    dbSettings: DBSettings.() -> DBSettings
) = create(DBPlatformSettings(), dbSettings)
