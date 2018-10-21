package com.dbflow5

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.dbflow5.config.DatabaseConfig
import com.dbflow5.config.FlowConfig
import com.dbflow5.config.FlowLog
import com.dbflow5.config.FlowManager
import com.dbflow5.database.AndroidSQLiteOpenHelper
import com.dbflow5.runtime.DirectModelNotifier
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class DBFlowTestRule : TestRule {

    val context: Context
        get() = ApplicationProvider.getApplicationContext()

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {

            @Throws(Throwable::class)
            override fun evaluate() {
                FlowLog.setMinimumLoggingLevel(FlowLog.Level.V)
                DirectModelNotifier().clearListeners()
                FlowManager.init(FlowConfig.Builder(context)
                        .database(DatabaseConfig.Builder(TestDatabase::class, AndroidSQLiteOpenHelper.createHelperCreator(context))
                                .transactionManagerCreator(::ImmediateTransactionManager2)
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