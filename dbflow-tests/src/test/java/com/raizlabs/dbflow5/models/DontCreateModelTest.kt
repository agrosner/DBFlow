package com.raizlabs.dbflow5.models

import com.raizlabs.dbflow5.BaseUnitTest
import com.raizlabs.dbflow5.assertThrowsException
import com.raizlabs.dbflow5.config.databaseForTable
import com.raizlabs.dbflow5.database.SQLiteException
import com.raizlabs.dbflow5.query.list
import com.raizlabs.dbflow5.query.select
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
