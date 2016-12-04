package com.raizlabs.android.dbflow.test.sqlcipher

import com.raizlabs.android.dbflow.structure.database.DatabaseHelperListener
import com.raizlabs.android.dbflow.config.DatabaseConfig
import com.raizlabs.android.dbflow.config.DatabaseDefinition
import com.raizlabs.android.dbflow.config.FlowConfig
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.structure.database.OpenHelper

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.robolectric.RuntimeEnvironment

/**
 * Description:
 */
class CipherTestRule private constructor() : TestRule {

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {

            @Throws(Throwable::class)
            override fun evaluate() {
                FlowManager.init(FlowConfig.Builder(RuntimeEnvironment.application)
                        .addDatabaseConfig(DatabaseConfig.Builder(CipherDatabase::class.java)
                                .openHelper { databaseDefinition, helperListener -> SQLCipherHelperImpl(databaseDefinition, helperListener) }.build()).build())
                try {
                    base.evaluate()
                } finally {
                    FlowManager.getDatabase(CipherDatabase::class.java).reset(RuntimeEnvironment.application)
                    FlowManager.destroy()
                }
            }
        }
    }

    companion object {

        fun create(): CipherTestRule {
            return CipherTestRule()
        }
    }
}
