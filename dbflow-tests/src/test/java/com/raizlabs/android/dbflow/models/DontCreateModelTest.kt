package com.raizlabs.android.dbflow.models

import android.database.sqlite.SQLiteException
import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.assertThrowsException
import com.raizlabs.android.dbflow.config.databaseForTable
import com.raizlabs.android.dbflow.query.list
import com.raizlabs.android.dbflow.query.select
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
