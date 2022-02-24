package com.dbflow5.sql.language

import com.dbflow5.TestDatabase_Database
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.query.delete
import com.dbflow5.query.operations.indexProperty
import com.dbflow5.query.select
import com.dbflow5.query.update
import com.dbflow5.simpleModelAdapter
import com.dbflow5.test.DatabaseTestRule
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class IndexedByTest {

    @get:Rule
    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun validateSelectQuery() = runBlockingTest {
        dbRule {
            val indexed = simpleModelAdapter.select() indexedBy
                indexProperty(
                    "Index",
                    false,
                    SimpleModel_Table.name
                )
            assertEquals("SELECT * FROM `SimpleModel` INDEXED BY `Index`", indexed.query.trim())
        }
    }

    @Test
    fun validateDeleteQuery() = runBlockingTest {
        dbRule {
            val indexed = simpleModelAdapter.delete() indexedBy
                indexProperty(
                    "Index",
                    false,
                    SimpleModel_Table.name
                )
            assertEquals("DELETE FROM `SimpleModel` INDEXED BY `Index`", indexed.query.trim())
        }
    }

    @Test
    fun validateUpdateQuery() = runBlockingTest {
        dbRule {
            val indexed = simpleModelAdapter.update() indexedBy
                indexProperty(
                    "Index",
                    false,
                    SimpleModel_Table.name
                )
            assertEquals("UPDATE `SimpleModel` INDEXED BY `Index`", indexed.query.trim())
        }
    }
}