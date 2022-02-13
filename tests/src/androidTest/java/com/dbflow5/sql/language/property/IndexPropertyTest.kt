package com.dbflow5.sql.language.property

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.dropIndex
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.query2.operations.indexProperty
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Test

class IndexPropertyTest : BaseUnitTest() {

    @Test
    fun validateIndexProperty() = runBlockingTest {
        database<TestDatabase> { db ->
            val prop = indexProperty(
                "Index", true,
                SimpleModel_Table.name
            )

            prop.index.execute(db)
            dropIndex(db, prop.name)
            assertEquals("`Index`", prop.name)
        }
    }
}