package com.dbflow5.test.sql.language

import com.dbflow5.query.delete
import com.dbflow5.query.operations.indexProperty
import com.dbflow5.query.select
import com.dbflow5.query.update
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.test.SimpleModel
import com.dbflow5.test.TestDatabase_Database
import com.dbflow5.test.TestRule
import kotlin.test.Test
import kotlin.test.assertEquals

class IndexedByTest : TestRule() {

    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun validateSelectQuery() = dbRule.runTest {
        val indexed = simpleModelAdapter.select() indexedBy
            indexProperty(
                "Index",
                false,
                SimpleModel.name
            )
        assertEquals("SELECT * FROM `SimpleModel` INDEXED BY `Index`", indexed.query.trim())
    }

    @Test
    fun validateDeleteQuery() = dbRule.runTest {
        val indexed = simpleModelAdapter.delete() indexedBy
            indexProperty(
                "Index",
                false,
                SimpleModel.name
            )
        assertEquals("DELETE FROM `SimpleModel` INDEXED BY `Index`", indexed.query.trim())
    }

    @Test
    fun validateUpdateQuery() = dbRule.runTest {
        val indexed = simpleModelAdapter.update() indexedBy
            indexProperty(
                "Index",
                false,
                SimpleModel.name
            )
        assertEquals("UPDATE `SimpleModel` INDEXED BY `Index`", indexed.query.trim())
    }
}