package com.raizlabs.android.dbflow.models

import android.database.sqlite.SQLiteException
import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.assertThrowsException
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.list
import com.raizlabs.android.dbflow.kotlinextensions.select
import org.junit.Test

/**
 * Description:
 */
class DontCreateModelTest : BaseUnitTest() {

    @Test
    fun testModelNotCreated() {
        assertThrowsException(SQLiteException::class) {
            (select from DontCreateModel::class).list
        }
    }
}
