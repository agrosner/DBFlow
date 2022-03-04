package com.dbflow5.test.sql.language

import com.dbflow5.test.TestDatabase_Database
import com.dbflow5.test.SimpleModel_Table
import com.dbflow5.query.delete
import com.dbflow5.query.operations.indexProperty
import com.dbflow5.query.select
import com.dbflow5.query.update
import com.dbflow5.test.simpleModelAdapter
import com.dbflow5.test.DatabaseTestRule
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class IndexedByTest {

    
    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun validateSelectQuery() = runTest {
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
    fun validateDeleteQuery() = runTest {
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
    fun validateUpdateQuery() = runTest {
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