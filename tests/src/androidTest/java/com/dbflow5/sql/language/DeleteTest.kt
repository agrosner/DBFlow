package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.config.writableTransaction
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.query2.delete
import com.dbflow5.query2.select
import com.dbflow5.query2.selectCountOf
import com.dbflow5.simpleModelAdapter
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import kotlin.test.assertTrue

class DeleteTest : BaseUnitTest() {

    @Test
    fun validateDeletion() = runBlockingTest {
        database<TestDatabase>().writableTransaction {
            simpleModelAdapter.save(SimpleModel("name"))
            assertTrue(simpleModelAdapter.delete().execute() > 0)
            assertFalse(simpleModelAdapter.selectCountOf().hasData())
        }
    }

    @Test
    fun validateDeletionWithQuery() = runBlockingTest {
        database<TestDatabase>().writableTransaction {
            simpleModelAdapter.saveAll(
                listOf(
                    SimpleModel("name"),
                    SimpleModel("another name"),
                )
            )

            val where = simpleModelAdapter.delete().where(SimpleModel_Table.name.`is`("name"))
            assertEquals("DELETE FROM `SimpleModel` WHERE `name`='name'", where.query.trim())
            assertTrue(where.execute() > 0)

            assertEquals(1, simpleModelAdapter.select().list().size)
        }
    }
}