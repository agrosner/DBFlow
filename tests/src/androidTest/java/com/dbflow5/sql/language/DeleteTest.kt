package com.dbflow5.sql.language

import com.dbflow5.TestDatabase_Database
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.query.delete
import com.dbflow5.query.select
import com.dbflow5.query.selectCountOf
import com.dbflow5.simpleModelAdapter
import com.dbflow5.test.DatabaseTestRule
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertTrue

class DeleteTest {

    @get:Rule
    val dbRule = DatabaseTestRule(TestDatabase_Database::create)

    @Test
    fun validateDeletion() = runBlockingTest {
        dbRule {
            simpleModelAdapter.save(SimpleModel("name"))
            assertTrue(simpleModelAdapter.delete().execute() > 0)
            assertFalse(simpleModelAdapter.selectCountOf().hasData())
        }
    }

    @Test
    fun validateDeletionWithQuery() = runBlockingTest {
        dbRule {
            simpleModelAdapter.saveAll(
                listOf(
                    SimpleModel("name"),
                    SimpleModel("another name"),
                )
            )

            val where = simpleModelAdapter.delete().where(SimpleModel_Table.name eq "name")
            assertEquals("DELETE FROM `SimpleModel` WHERE `name` = 'name'", where.query.trim())
            assertTrue(where.execute() > 0)

            assertEquals(1, simpleModelAdapter.select().list().size)
        }
    }
}