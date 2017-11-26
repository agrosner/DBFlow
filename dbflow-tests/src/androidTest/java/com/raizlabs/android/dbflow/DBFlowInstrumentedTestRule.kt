package com.raizlabs.android.dbflow

import com.raizlabs.android.dbflow.config.DatabaseConfig
import com.raizlabs.android.dbflow.config.DatabaseDefinition
import com.raizlabs.android.dbflow.config.FlowConfig
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.prepackaged.PrepackagedDB
import com.raizlabs.android.dbflow.sqlcipher.CipherDatabase
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class DBFlowInstrumentedTestRule : TestRule {

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {

            @Throws(Throwable::class)
            override fun evaluate() {
                FlowManager.init(FlowConfig.Builder(DemoApp.context)
                        .addDatabaseConfig(DatabaseConfig(
                                databaseClass = AppDatabase::class.java,
                                transactionManagerCreator = { databaseDefinition: DatabaseDefinition ->
                                    ImmediateTransactionManager(databaseDefinition)
                                }))
                        .addDatabaseConfig(DatabaseConfig(
                                databaseClass = PrepackagedDB::class.java,
                                databaseName = "prepackaged"))
                        .addDatabaseConfig(DatabaseConfig(
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