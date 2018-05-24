package com.dbflow5

import android.content.Context
import com.dbflow5.config.DatabaseConfig
import com.dbflow5.config.FlowConfig
import com.dbflow5.config.FlowLog
import com.dbflow5.config.FlowManager
import com.dbflow5.database.AndroidSQLiteOpenHelper
import com.dbflow5.provider.ContentDatabase
import com.dbflow5.runtime.DirectModelNotifier
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
                    .database(DatabaseConfig.Builder(TestDatabase::class, AndroidSQLiteOpenHelper.createHelperCreator(context))
                        .transactionManagerCreator(::ImmediateTransactionManager2)
                        .build())
                    .database(DatabaseConfig.builder(ContentDatabase::class,
                        AndroidSQLiteOpenHelper.createHelperCreator(context))
                        .databaseName("content")
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