package com.dbflow5.test.config

import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.database.config.DBSettings
import com.dbflow5.test.helpers.platformSettings
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DBFlowDatabaseTest {


    @Test
    fun `validate db opened`() {
        val db = TestDBFlowDatabase()
        db.writableDatabase
        assertTrue(db.isOpen)
    }

    @Test
    fun `validate can close db`() {
        val db = TestDBFlowDatabase()
        db.writableDatabase
        assertTrue(db.isOpen)

        db.close()
        assertFalse(db.isOpen)

        db.writableDatabase
        assertTrue(db.isOpen)
    }
}

private class TestDBFlowDatabase(
    override val databaseVersion: Int = 1,
    override val isForeignKeysSupported: Boolean = true,
    override val tables: List<KClass<*>> = listOf(),
    override val views: List<KClass<*>> = listOf(),
    override val queries: List<KClass<*>> = listOf(),
    override val settings: DBSettings = DBSettings(
        name = "TestDB",
        platformSettings = platformSettings())) : DBFlowDatabase<TestDBFlowDatabase>()
