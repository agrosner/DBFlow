package com.dbflow5.test

import com.dbflow5.config.FlowLog
import com.dbflow5.database.DBFlowDatabase
import com.dbflow5.database.DatabaseObjectLookup
import com.dbflow5.database.GeneratedDatabaseHolderFactory
import com.dbflow5.database.config.DBCreator
import com.dbflow5.database.config.DBSettings
import com.dbflow5.mpp.use
import com.dbflow5.observing.notifications.ModelNotifier
import com.dbflow5.test.helpers.platformSettings
import kotlinx.coroutines.runBlocking

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
        copy(
            transactionDispatcherFactory = TestTransactionDispatcherFactory(),
            modelNotifierFactory = { ModelNotifier.Default },
            inMemory = true,
        )
    },
) {

    lateinit var db: DB

    @Suppress("UNCHECKED_CAST")
    inline operator fun invoke(fn: DB.() -> Unit) {
        acquireFreshDatabase {
            db.apply { fn() }
        }
    }

    fun runTest(fn: suspend DB.() -> Unit) {
        acquireFreshDatabase {
            runBlocking {
                db.fn()
            }
        }
    }

    inline fun acquireFreshDatabase(fn: () -> Unit) {
        DatabaseObjectLookup.loadHolder(GeneratedDatabaseHolderFactory)
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
