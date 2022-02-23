package com.dbflow5.test

import androidx.test.platform.app.InstrumentationRegistry
import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.config.FlowLog
import com.dbflow5.config.FlowManager
import com.dbflow5.config.GeneratedDatabaseHolderFactory
import com.dbflow5.database.scope.WritableDatabaseScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Provides hook into specified DB.
 */
class DatabaseTestRule<DB : DBFlowDatabase>(private val creator: () -> DB) :
    TestRule {

    private lateinit var db: DB

    suspend operator fun <R> invoke(fn: suspend WritableDatabaseScope<DB>.() -> R) {
        WritableDatabaseScope(db).apply { fn() }
    }

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {

            @Throws(Throwable::class)
            override fun evaluate() {
                FlowManager.init(InstrumentationRegistry.getInstrumentation().targetContext) {
                    addDatabaseHolder(GeneratedDatabaseHolderFactory)
                }
                Dispatchers.setMain(TestCoroutineDispatcher())
                FlowLog.setMinimumLoggingLevel(FlowLog.Level.V)
                creator().use {
                    db = it
                    try {
                        base.evaluate()
                    } finally {
                        db.close()
                    }
                }
            }
        }
    }
}