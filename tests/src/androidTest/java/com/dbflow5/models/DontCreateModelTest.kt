package com.dbflow5.models

import com.dbflow5.TestDatabase_Database
import com.dbflow5.assertThrowsException
import com.dbflow5.database.SQLiteException
import com.dbflow5.dontCreateModelAdapter
import com.dbflow5.query.select
import com.dbflow5.test.DatabaseTestRule
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test

/**
 * Description:
 */
class DontCreateModelTest {

    @get:Rule
    val dbRule = DatabaseTestRule(TestDatabase_Database::create)

    @Test
    fun testModelNotCreated() = runBlockingTest {
        dbRule {
            assertThrowsException(SQLiteException::class) {
                dontCreateModelAdapter.select().list()
            }
        }
    }
}
