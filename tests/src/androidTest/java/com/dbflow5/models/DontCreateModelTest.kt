package com.dbflow5.models

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.assertThrowsException
import com.dbflow5.config.database
import com.dbflow5.config.readableTransaction
import com.dbflow5.database.SQLiteException
import com.dbflow5.query.select
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

/**
 * Description:
 */
class DontCreateModelTest : BaseUnitTest() {

    @Test
    fun testModelNotCreated() = runBlockingTest {
        database<TestDatabase>().readableTransaction {
            assertThrowsException(SQLiteException::class) {
                (select from DontCreateModel::class).queryList()
            }
        }
    }
}
