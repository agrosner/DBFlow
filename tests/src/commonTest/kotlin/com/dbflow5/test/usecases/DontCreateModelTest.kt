package com.dbflow5.test.usecases

import com.dbflow5.database.SQLiteException
import com.dbflow5.query.select
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.test.TestDatabase_Database
import com.dbflow5.test.assertThrowsException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * Description:
 */
class DontCreateModelTest {

    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun testModelNotCreated() = runTest {
        dbRule {
            assertThrowsException(SQLiteException::class) {
                dontCreateModelAdapter.select().list()
            }
        }
    }
}
