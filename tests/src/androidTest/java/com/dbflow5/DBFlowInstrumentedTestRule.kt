package com.dbflow5

import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.config.FlowLog
import com.dbflow5.config.FlowManager
import com.dbflow5.config.flowConfig
import com.dbflow5.database.AndroidSQLiteOpenHelper
import com.dbflow5.prepackaged.PrepackagedDB
import com.dbflow5.provider.ContentDatabase
import com.dbflow5.runtime.ContentResolverNotifier
import com.dbflow5.sqlcipher.CipherDatabase
import com.dbflow5.sqlcipher.SQLCipherOpenHelper
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement


class DBFlowInstrumentedTestRule : TestRule {

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {

            @Throws(Throwable::class)
            override fun evaluate() {
                FlowLog.setMinimumLoggingLevel(FlowLog.Level.V)
                FlowManager.init(flowConfig(DemoApp.context) {
                    database<AppDatabase>({
                        modelNotifier(ContentResolverNotifier(DemoApp.context, "com.grosner.content"))
                        transactionManagerCreator { databaseDefinition: DBFlowDatabase ->
                            ImmediateTransactionManager(databaseDefinition)
                        }
                    }, AndroidSQLiteOpenHelper.createHelperCreator(DemoApp.context))
                    database<PrepackagedDB>({
                        databaseName("prepackaged")
                    }, AndroidSQLiteOpenHelper.createHelperCreator(DemoApp.context))
                    database<CipherDatabase>(openHelperCreator = SQLCipherOpenHelper.createHelperCreator(DemoApp.context, "dbflow-rules"))
                    database<ContentDatabase>({
                        databaseName("content")
                    }, AndroidSQLiteOpenHelper.createHelperCreator(DemoApp.context))
                    database<TestDatabase>({
                        transactionManagerCreator(::ImmediateTransactionManager)
                    }, AndroidSQLiteOpenHelper.createHelperCreator(DemoApp.context))
                })
                try {
                    base.evaluate()
                } finally {
                    FlowManager.destroy()
                }
            }
        }
    }

    companion object {
        fun create() = DBFlowInstrumentedTestRule()
    }
}