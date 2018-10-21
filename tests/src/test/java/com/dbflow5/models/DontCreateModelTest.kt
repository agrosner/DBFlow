package com.dbflow5.models

import com.dbflow5.BaseUnitTest
import com.dbflow5.assertThrowsException
import com.dbflow5.config.databaseForTable
import com.dbflow5.database.SQLiteException
import com.dbflow5.query.list
import com.dbflow5.query.select
import org.junit.Test

/**
 * Description:
 */
class DontCreateModelTest : BaseUnitTest() {

    @Test
    fun testModelNotCreated() {
        databaseForTable<DontCreateModel> {
            assertThrowsException(SQLiteException::class) {
                (select from DontCreateModel::class).list
            }
        }
    }
}
