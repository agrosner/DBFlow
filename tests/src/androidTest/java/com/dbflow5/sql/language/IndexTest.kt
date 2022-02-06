package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.models.TwoColumnModel_Table
import com.dbflow5.query2.createIndexOn
import org.junit.Assert.assertEquals
import org.junit.Test

class IndexTest : BaseUnitTest() {

    private val simpleModelAdapter
        get() = database<TestDatabase>().simpleModelAdapter

    private val twoColumnModelAdapter
        get() = database<TestDatabase>().twoColumnModelAdapter

    @Test
    fun validateBasicIndex() {
        assertEquals(
            "CREATE INDEX IF NOT EXISTS `index` ON `SimpleModel`(`name`)",
            simpleModelAdapter.createIndexOn(
                name = "index",
                property = SimpleModel_Table.name
            ).query
        )
    }

    @Test
    fun validateUniqueIndex() {
        assertEquals(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index` ON `TwoColumnModel`(`name`, `id`)",
            twoColumnModelAdapter.createIndexOn(
                name = "index",
                TwoColumnModel_Table.name,
                TwoColumnModel_Table.id,
            )
                .unique()
                .query
        )
    }
}