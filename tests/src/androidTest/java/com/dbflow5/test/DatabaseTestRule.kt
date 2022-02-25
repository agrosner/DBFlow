package com.dbflow5.test

import androidx.test.platform.app.InstrumentationRegistry
import com.dbflow5.TestTransactionDispatcherFactory
import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.config.FlowLog
import com.dbflow5.config.DatabaseObjectLookup
import com.dbflow5.config.GeneratedDatabaseHolderFactory
import com.dbflow5.database.config.DBCreator
import com.dbflow5.database.config.DBSettings
import com.dbflow5.database.scope.WritableDatabaseScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

data class TestDatabaseScope<DB : DBFlowDatabase>(
    private val dbScope: WritableDatabaseScope<DB>,
    private val testScope: TestCoroutineScope
) : WritableDatabaseScope<DB> by dbScope, TestCoroutineScope by testScope

/**
 * Provides hook into specified DB.
 */
class DatabaseTestRule<DB : DBFlowDatabase>(
    private val creator: DBCreator<DB>,
    /**
     * Injects [TestTransactionDispatcherFactory] for settings. Typically don't override
     * unless you want to change this field.
     */
    private val defaultSettingsCopy: DBSettings.() -> DBSettings = {
        copy(transactionDispatcherFactory = TestTransactionDispatcherFactory())
    },
) :
    TestRule {

    lateinit var db: DB

    @Suppress("UNCHECKED_CAST")
    inline operator fun invoke(fn: WritableDatabaseScope<DB>.() -> Unit) {
        (db.writableScope as WritableDatabaseScope<DB>).apply { fn() }
    }

    fun runBlockingTest(fn: suspend TestDatabaseScope<DB>.() -> Unit) {
        kotlinx.coroutines.test.runBlockingTest {
            TestDatabaseScope(WritableDatabaseScope(db), this).apply { fn() }
        }
    }

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {

            @Throws(Throwable::class)
            override fun evaluate() {
                DatabaseObjectLookup.loadHolder(GeneratedDatabaseHolderFactory)
                Dispatchers.setMain(TestCoroutineDispatcher())
                FlowLog.setMinimumLoggingLevel(FlowLog.Level.V)
                creator.create(
                    InstrumentationRegistry.getInstrumentation().targetContext,
                    defaultSettingsCopy
                ).use {
                    db = it
                    try {
                        base.evaluate()
                    } finally {
                        db.destroy()
                    }
                }
            }
        }
    }
}