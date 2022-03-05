package com.dbflow5.test

import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.config.DatabaseObjectLookup
import com.dbflow5.config.FlowLog
import com.dbflow5.config.GeneratedDatabaseHolderFactory
import com.dbflow5.database.config.DBCreator
import com.dbflow5.database.config.DBSettings
import com.dbflow5.database.scope.WritableDatabaseScope
import com.dbflow5.mpp.use
import com.dbflow5.test.helpers.platformSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain

data class TestDatabaseScope<DB : DBFlowDatabase>(
    private val dbScope: WritableDatabaseScope<DB>,
    val testScope: TestScope
) : WritableDatabaseScope<DB> by dbScope, CoroutineScope by testScope

/**
 * Provides hook into specified DB.
 */
class DatabaseTestRule<DB : DBFlowDatabase>(
    val creator: DBCreator<DB>,
    /**
     * Injects [TestTransactionDispatcherFactory] for settings. Typically don't override
     * unless you want to change this field.
     */
    val defaultSettingsCopy: DBSettings.() -> DBSettings = {
        copy(transactionDispatcherFactory = TestTransactionDispatcherFactory())
    },
) {

    lateinit var db: DB

    @Suppress("UNCHECKED_CAST")
    inline operator fun invoke(fn: WritableDatabaseScope<DB>.() -> Unit) {
        acquireFreshDatabase {
            (db.writableScope as WritableDatabaseScope<DB>).apply { fn() }
        }
    }

    fun runTest(fn: suspend TestDatabaseScope<DB>.() -> Unit) {
        acquireFreshDatabase {
            kotlinx.coroutines.test.runTest {
                TestDatabaseScope(WritableDatabaseScope(db), this).apply { fn() }
            }
        }
    }

    inline fun acquireFreshDatabase(fn: () -> Unit) {
        DatabaseObjectLookup.loadHolder(GeneratedDatabaseHolderFactory)
        Dispatchers.setMain(UnconfinedTestDispatcher())
        FlowLog.setMinimumLoggingLevel(FlowLog.Level.V)
        creator.create(
            platformSettings(),
            defaultSettingsCopy
        ).use {
            it.destroy()
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
