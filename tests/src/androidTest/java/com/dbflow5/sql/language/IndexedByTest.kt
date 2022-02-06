package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.config.readableTransaction
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.query.property.IndexProperty
import com.dbflow5.query2.delete
import com.dbflow5.query2.select
import com.dbflow5.query2.update
import com.dbflow5.simpleModelAdapter
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Test

class IndexedByTest : BaseUnitTest() {

    @Test
    fun validateSelectQuery() = runBlockingTest {
        val database = database<TestDatabase>()
        val indexed = database.readableTransaction {
            simpleModelAdapter.select() indexedBy
                IndexProperty(
                    "Index",
                    false,
                    simpleModelAdapter,
                    SimpleModel_Table.name
                )
        }
        assertEquals("SELECT * FROM `SimpleModel` INDEXED BY `Index`", indexed.query.trim())
    }

    @Test
    fun validateDeleteQuery() = runBlockingTest {
        val database = database<TestDatabase>()
        val indexed = database.readableTransaction {
            simpleModelAdapter.delete() indexedBy
                IndexProperty(
                    "Index",
                    false,
                    simpleModelAdapter,
                    SimpleModel_Table.name
                )
        }
        assertEquals("DELETE FROM `SimpleModel` INDEXED BY `Index`", indexed.query.trim())
    }

    @Test
    fun validateUpdateQuery() = runBlockingTest {
        val database = database<TestDatabase>()
        val indexed = database.readableTransaction {
            simpleModelAdapter.update() indexedBy
                IndexProperty(
                    "Index",
                    false,
                    simpleModelAdapter,
                    SimpleModel_Table.name
                )
        }
        assertEquals("UPDATE `SimpleModel` INDEXED BY `Index`", indexed.query.trim())
    }
}