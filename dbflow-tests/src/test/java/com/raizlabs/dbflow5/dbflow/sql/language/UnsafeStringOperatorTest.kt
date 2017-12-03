package com.raizlabs.dbflow5.dbflow.sql.language

import com.raizlabs.dbflow5.dbflow.BaseUnitTest
import com.raizlabs.dbflow5.dbflow.assertEquals
import com.raizlabs.dbflow5.config.databaseForTable
import com.raizlabs.dbflow5.dbflow.models.SimpleModel
import com.raizlabs.dbflow5.query.UnSafeStringOperator
import org.junit.Test

class UnsafeStringOperatorTest : BaseUnitTest() {

    @Test
    fun testCanIncludeInQuery() {
        databaseForTable<SimpleModel> {
            val op = UnSafeStringOperator("name = ?, id = ?, test = ?", arrayOf("'name'", "0", "'test'"))
            assertEquals("name = 'name', id = 0, test = 'test'", op)
            assertEquals("SELECT * FROM `SimpleModel` WHERE name = 'name', id = 0, test = 'test'",
                    select from SimpleModel::class where op)
        }
    }
}