package com.dbflow5.test

import com.dbflow5.database.DBFlowDatabase
import com.dbflow5.config.DatabaseObjectLookup
import com.dbflow5.config.FlowLog
import com.dbflow5.database.GeneratedDatabaseHolderFactory
import com.dbflow5.database.config.DBCreator
import com.dbflow5.database.config.DBSettings
import com.dbflow5.mpp.use
import com.dbflow5.observing.notifications.DirectModelNotifier
import com.dbflow5.test.helpers.platformSettings
import kotlinx.coroutines.test.TestScope

/**
 * Provides hook into specified DB.
 */
class DatabaseTestRule<DB : DBFlowDatabase<DB>>(
    val creator: DBCreator<DB>,
    /**
     * Injects [TestTransactionDispatcherFactory] for settings. Typically don't override
     * unless you want to change this field.
     */
    val defaultSettingsCopy: DBSettings.() -> DBSettings = {
        copy(transactionDispatcherFactory = TestTransactionDispatcherFactory(),
            modelNotifierFactory = { DirectModelNotifier(notificationScope = TestScope()) })
    },
) {

    lateinit var db: DB

    @Suppress("UNCHECKED_CAST")
    inline operator fun invoke(fn: DB.() -> Unit) {
        acquireFreshDatabase {
            db.apply { fn() }
        }
    }

    fun runTest(fn: suspend DB.(testScope: TestScope) -> Unit) {
        acquireFreshDatabase {
            kotlinx.coroutines.test.runTest {
                val scope = this
                db.apply { fn(scope) }
            }
        }
    }

    inline fun acquireFreshDatabase(fn: () -> Unit) {
        DatabaseObjectLookup.loadHolder(GeneratedDatabaseHolderFactory)
        //Dispatchers.setMain(UnconfinedTestDispatcher())
        FlowLog.setMinimumLoggingLevel(FlowLog.Level.V)
        creator.create(
            platformSettings(),
            defaultSettingsCopy
        ).use {
            db = it
            // force creations first
            db.writableDatabase
            try {
                fn()
            } finally {
                db.destroy()
            }
        }
    }
}
