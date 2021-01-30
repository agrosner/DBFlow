package com.dbflow5

import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.config.DatabaseConfig
import com.dbflow5.config.FlowConfig
import com.dbflow5.config.FlowLog
import com.dbflow5.config.FlowManager
import com.dbflow5.database.AndroidSQLiteOpenHelper
import com.dbflow5.prepackaged.PrepackagedDB
import com.dbflow5.provider.ContentDatabase
import com.dbflow5.runtime.ContentResolverNotifier
import com.dbflow5.sqlcipher.CipherDatabase
import com.dbflow5.sqlcipher.SQLCipherOpenHelperImpl
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement


class DBFlowInstrumentedTestRule : TestRule {

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {

            @Throws(Throwable::class)
            override fun evaluate() {
                FlowLog.setMinimumLoggingLevel(FlowLog.Level.V)
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
                        .database(DatabaseConfig.builder(CipherDatabase::class) { db, callback ->
                            SQLCipherOpenHelperImpl(DemoApp.context, db, callback)
                        }.build())
                        .database(DatabaseConfig.builder(ContentDatabase::class,
                                AndroidSQLiteOpenHelper.createHelperCreator(DemoApp.context))
                                .databaseName("content")
                                .build())
                        .database(DatabaseConfig.Builder(TestDatabase::class, AndroidSQLiteOpenHelper.createHelperCreator(DemoApp.context))
                                .transactionManagerCreator(::ImmediateTransactionManager)
                                .build())
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