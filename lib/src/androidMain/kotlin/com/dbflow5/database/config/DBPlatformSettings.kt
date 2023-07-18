package com.dbflow5.database.config

import android.app.ActivityManager
import android.content.Context
import com.dbflow5.database.DBFlowDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

actual data class DBPlatformSettings(
    val context: Context,
    actual val transactionCoroutineDispatcher: CoroutineDispatcher =
        Executors.newSingleThreadExecutor().asCoroutineDispatcher(),
    actual val callbackDispatcher: CoroutineDispatcher = Dispatchers.Main
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
fun <DB : DBFlowDatabase<DB>> DBCreator<DB>.create(
    context: Context,
    dbSettings: DBSettings.() -> DBSettings
) = create(DBPlatformSettings(context), dbSettings)
