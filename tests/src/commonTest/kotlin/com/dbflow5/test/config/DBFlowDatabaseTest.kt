package com.dbflow5.test.config

import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.database.config.DBCreator
import com.dbflow5.database.config.DBPlatformSettings
import com.dbflow5.database.config.DBSettings
import com.dbflow5.test.DatabaseTestRule
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DBFlowDatabaseTest {

    private val dbRule = DatabaseTestRule(object : DBCreator<TestDBFlowDatabase> {
        override fun create(
            platformSettings: DBPlatformSettings,
            settingsFn: DBSettings.() -> DBSettings
        ): TestDBFlowDatabase {
            return TestDBFlowDatabase(
                settings = DBSettings(
                    name = "TestDB",
                    platformSettings = platformSettings
                ).settingsFn()
            )
        }
    })


    @Test
    fun `validate db opened`() = dbRule.runTest {
        writableDatabase
        assertTrue(isOpen)
    }

    @Test
    fun `validate can close db`() = dbRule.runTest {
        writableDatabase
        assertTrue(isOpen)

        close()
        assertFalse(isOpen)

        writableDatabase
        assertTrue(isOpen)
    }
}

private class TestDBFlowDatabase(
    override val databaseVersion: Int = 1,
    override val isForeignKeysSupported: Boolean = true,
    override val tables: List<KClass<*>> = listOf(),
    override val views: List<KClass<*>> = listOf(),
    override val queries: List<KClass<*>> = listOf(),
    override val settings: DBSettings,
) : DBFlowDatabase<TestDBFlowDatabase>()
