package com.dbflow5

import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.config.FlowConfig
import com.dbflow5.config.FlowLog
import com.dbflow5.config.FlowManager
import com.dbflow5.contentobserver.ContentObserverDatabase
import com.dbflow5.database.AndroidSQLiteOpenHelper
import com.dbflow5.runtime.ContentResolverNotifier
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement


class DBFlowInstrumentedTestRule(private val dbConfigBlock: FlowConfig.Builder.() -> Unit) : TestRule {

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {

            @Throws(Throwable::class)
            override fun evaluate() {
                FlowLog.setMinimumLoggingLevel(FlowLog.Level.V)
                FlowManager.init(DemoApp.context) {
                    database<TestDatabase>({
                        transactionManagerCreator(::ImmediateTransactionManager)
                    }, AndroidSQLiteOpenHelper.createHelperCreator(DemoApp.context))
                    dbConfigBlock()
                }
                try {
                    base.evaluate()
                } finally {
                    FlowManager.destroy()
                }
            }
        }
    }

    companion object {
        fun create(dbConfigBlock: FlowConfig.Builder.() -> Unit = {}) = DBFlowInstrumentedTestRule(dbConfigBlock)
    }
}