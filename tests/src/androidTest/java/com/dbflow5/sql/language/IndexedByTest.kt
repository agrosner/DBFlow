package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.config.readableTransaction
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.query.property.IndexProperty
import com.dbflow5.query.select
import com.dbflow5.simpleModelAdapter
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Test

class IndexedByTest : BaseUnitTest() {

    @Test
    fun validateQuery() = runBlockingTest {
        val database = database<TestDatabase>()
        val indexed = database.readableTransaction {
            (select from simpleModelAdapter)
                .indexedBy(
                    IndexProperty(
                        "Index",
                        false,
                        simpleModelAdapter,
                        SimpleModel_Table.name
                    )
                )
        }
        assertEquals("SELECT * FROM `SimpleModel` INDEXED BY `Index`", indexed.query.trim())
    }
}