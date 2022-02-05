package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.config.writableTransaction
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.query.delete
import com.dbflow5.query.select
import com.dbflow5.simpleModelAdapter
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class DeleteTest : BaseUnitTest() {

    @Test
    fun validateQuery() {
        assertEquals("DELETE ", delete().query)
    }

    @Test
    fun validateDeletion() = runBlockingTest {
        database<TestDatabase>().writableTransaction {
            simpleModelAdapter.save(SimpleModel("name"))
            simpleModelAdapter.delete().execute()
            assertFalse((select from simpleModelAdapter).hasData())
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
            where.execute()

            assertEquals(1, (select from simpleModelAdapter).queryList().size)
        }
    }
}