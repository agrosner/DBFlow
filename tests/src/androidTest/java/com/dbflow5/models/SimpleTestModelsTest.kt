package com.dbflow5.models

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Description:
 */
class SimpleTestModelsTest : BaseUnitTest() {

    @Test
    fun validateCreationQuery() {
        assertEquals(
            "CREATE TABLE IF NOT EXISTS `TypeConverterModel`(" +
                "`id` INTEGER NOT NULL ON CONFLICT FAIL, " +
                "`opaqueData` BLOB, " +
                "`blob` BLOB, " +
                "`customType` INTEGER, " +
                "PRIMARY KEY(`id`, `customType`))",
            database<TestDatabase>().typeConverterModelAdapter.creationSQL.query
        )
    }
}