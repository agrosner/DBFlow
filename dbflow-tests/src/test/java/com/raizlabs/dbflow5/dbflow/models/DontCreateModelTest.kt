package com.raizlabs.dbflow5.dbflow.models

import android.database.sqlite.SQLiteException
import com.raizlabs.dbflow5.dbflow.BaseUnitTest
import com.raizlabs.dbflow5.dbflow.assertThrowsException
import com.raizlabs.dbflow5.config.databaseForTable
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
