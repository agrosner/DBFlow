package com.raizlabs.dbflow5

import android.content.Context
import com.raizlabs.dbflow5.config.DatabaseConfig
import com.raizlabs.dbflow5.config.FlowConfig
import com.raizlabs.dbflow5.config.FlowLog
import com.raizlabs.dbflow5.config.FlowManager
import com.raizlabs.dbflow5.database.AndroidSQLiteOpenHelper
import com.raizlabs.dbflow5.provider.ContentDatabase
import com.raizlabs.dbflow5.runtime.DirectModelNotifier
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.robolectric.RuntimeEnvironment

class DBFlowTestRule : TestRule {

    val context: Context
        get() = RuntimeEnvironment.application

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {

            @Throws(Throwable::class)
            override fun evaluate() {
                FlowLog.setMinimumLoggingLevel(FlowLog.Level.V)
                DirectModelNotifier().clearListeners()
                FlowManager.init(FlowConfig.Builder(RuntimeEnvironment.application)
                    .database(DatabaseConfig.Builder(
                        databaseClass = TestDatabase::class,
                        databaseName = "TestDatabase",
                        openHelperCreator = AndroidSQLiteOpenHelper.createHelperCreator(context))
                        .transactionManagerCreator(::ImmediateTransactionManager2)
                        .build())
                    .database(DatabaseConfig.builder(
                        database = ContentDatabase::class,
                        databaseName = "Content",
                        openHelperCreator = AndroidSQLiteOpenHelper.createHelperCreator(context))
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
        fun create() = DBFlowTestRule()
    }
}