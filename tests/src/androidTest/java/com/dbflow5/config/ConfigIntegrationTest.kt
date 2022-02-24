package com.dbflow5.config

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.dbflow5.TestDatabase
import com.dbflow5.database.AndroidSQLiteOpenHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ConfigIntegrationTest {

    val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Before
    fun setup() {
        FlowManager.close()
        FlowLog.setMinimumLoggingLevel(FlowLog.Level.V)
    }

    @Test
    fun test_flowConfig() {
        val config = flowConfig(context) {
            openDatabasesOnInit(true)
        }
        assertEquals(config.openDatabasesOnInit, true)
        assertTrue(config.databaseConfigMap.isEmpty())
        assertTrue(config.databaseHolders.isEmpty())
    }

    @Test
    fun test_tableConfig() {
        FlowManager.init(
            flowConfig(context) {
                database<TestDatabase>(
                    openHelperCreator = AndroidSQLiteOpenHelper.createHelperCreator(
                        context
                    )
                )
            })

        val flowConfig = FlowManager.getConfig()
        assertNotNull(flowConfig)

        val databaseConfig = flowConfig.databaseConfigMap[TestDatabase::class] as DatabaseConfig
        assertNotNull(databaseConfig)
    }

}


