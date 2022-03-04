package com.dbflow5.models

import com.dbflow5.TestDatabase_Database
import com.dbflow5.assertThrowsException
import com.dbflow5.database.SQLiteException
import com.dbflow5.dontCreateModelAdapter
import com.dbflow5.query.select
import com.dbflow5.test.DatabaseTestRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
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
