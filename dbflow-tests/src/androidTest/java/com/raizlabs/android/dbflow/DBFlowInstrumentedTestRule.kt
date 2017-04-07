package com.raizlabs.android.dbflow

import com.raizlabs.android.dbflow.config.DatabaseConfig
import com.raizlabs.android.dbflow.config.FlowConfig
import com.raizlabs.android.dbflow.config.FlowManager
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class DBFlowInstrumentedTestRule : TestRule {

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {

            @Throws(Throwable::class)
            override fun evaluate() {
                FlowManager.init(FlowConfig.Builder(DemoApp.context)
                    .addDatabaseConfig(DatabaseConfig.Builder(AppDatabase::class.java)
                        .transactionManagerCreator(::ImmediateTransactionManager)
                        .build()).build())
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