package com.dbflow5.test.sql.language

import com.dbflow5.query.delete
import com.dbflow5.query.select
import com.dbflow5.query.selectCountOf
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.test.SimpleModel
import com.dbflow5.test.SimpleModel_Table
import com.dbflow5.test.TestDatabase_Database
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeleteTest {

    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun validateDeletion() = runTest {
        dbRule {
            simpleModelAdapter.save(SimpleModel("name"))
            assertTrue(simpleModelAdapter.delete().execute() > 0)
            assertFalse(simpleModelAdapter.selectCountOf().hasData())
        }
    }

    @Test
    fun validateDeletionWithQuery() = runTest {
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