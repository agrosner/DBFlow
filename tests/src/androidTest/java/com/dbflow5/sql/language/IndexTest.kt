package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.query.indexOn
import com.dbflow5.query.nameAlias
import org.junit.Assert.assertEquals
import org.junit.Test

class IndexTest : BaseUnitTest() {

    private val simpleModelAdapter
        get() = database<TestDatabase>().simpleModelAdapter

    @Test
    fun validateBasicIndex() {
        assertEquals(
            "CREATE INDEX IF NOT EXISTS `index` ON `SimpleModel`(`name`)",
            indexOn(
                "index",
                simpleModelAdapter, SimpleModel_Table.name
            ).query
        )
    }

    @Test
    fun validateUniqueIndex() {
        assertEquals(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index` ON `SimpleModel`(`name`, `test`)",
            indexOn("index", simpleModelAdapter)
                .unique(true)
                .and(SimpleModel_Table.name)
                .and("test".nameAlias).query
        )
    }

    @Test
    fun validateBasicIndexNameAlias() {
        assertEquals(
            "CREATE INDEX IF NOT EXISTS `index` ON `SimpleModel`(`name`, `test`)",
            indexOn(
                "index",
                simpleModelAdapter,
                "name".nameAlias,
                "test".nameAlias
            ).query
        )
    }
}