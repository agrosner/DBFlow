package com.raizlabs.dbflow5

import com.raizlabs.dbflow5.config.DatabaseConfig
import com.raizlabs.dbflow5.config.FlowConfig
import com.raizlabs.dbflow5.config.FlowManager
import com.raizlabs.dbflow5.provider.ContentDatabase
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.robolectric.RuntimeEnvironment

class DBFlowTestRule : TestRule {

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {

            @Throws(Throwable::class)
            override fun evaluate() {
                FlowManager.init(FlowConfig.Builder(RuntimeEnvironment.application)
                        .database(DatabaseConfig.Builder(TestDatabase::class)
                                .transactionManagerCreator(::ImmediateTransactionManager2)
                                .build())
                        .database(DatabaseConfig.builder(ContentDatabase::class)
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