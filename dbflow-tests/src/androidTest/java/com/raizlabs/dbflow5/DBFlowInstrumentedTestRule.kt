package com.raizlabs.dbflow5

import com.raizlabs.dbflow5.config.DBFlowDatabase
import com.raizlabs.dbflow5.config.DatabaseConfig
import com.raizlabs.dbflow5.config.FlowConfig
import com.raizlabs.dbflow5.config.FlowManager
import com.raizlabs.dbflow5.database.AndroidSQLiteOpenHelper
import com.raizlabs.dbflow5.prepackaged.PrepackagedDB
import com.raizlabs.dbflow5.runtime.ContentResolverNotifier
import com.raizlabs.dbflow5.sqlcipher.CipherDatabase
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class DBFlowInstrumentedTestRule : TestRule {

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {

            @Throws(Throwable::class)
            override fun evaluate() {
                FlowManager.init(FlowConfig.Builder(DemoApp.context)
                    .database(DatabaseConfig(
                        databaseClass = AppDatabase::class.java,
                        openHelperCreator = AndroidSQLiteOpenHelper.createHelperCreator(DemoApp.context),
                        modelNotifier = ContentResolverNotifier(DemoApp.context, "com.grosner.content"),
                        transactionManagerCreator = { databaseDefinition: DBFlowDatabase ->
                            ImmediateTransactionManager(databaseDefinition)
                        }))
                    .database(DatabaseConfig(
                        openHelperCreator = AndroidSQLiteOpenHelper.createHelperCreator(DemoApp.context),
                        databaseClass = PrepackagedDB::class.java,
                        databaseName = "prepackaged"))
                    .database(DatabaseConfig(
                        openHelperCreator = AndroidSQLiteOpenHelper.createHelperCreator(DemoApp.context),
                        databaseClass = CipherDatabase::class.java))
                    .build())
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