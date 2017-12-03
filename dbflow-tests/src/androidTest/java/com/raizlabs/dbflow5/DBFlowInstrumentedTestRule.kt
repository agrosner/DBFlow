package com.raizlabs.dbflow5

import com.raizlabs.dbflow5.config.DatabaseConfig
import com.raizlabs.dbflow5.config.DatabaseDefinition
import com.raizlabs.dbflow5.config.FlowConfig
import com.raizlabs.dbflow5.config.FlowManager
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
                        .addDatabaseConfig(DatabaseConfig(
                                databaseClass = AppDatabase::class.java,
                                modelNotifier = ContentResolverNotifier("com.grosner.content"),
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