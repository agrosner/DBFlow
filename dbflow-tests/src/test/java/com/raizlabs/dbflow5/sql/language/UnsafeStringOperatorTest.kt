package com.raizlabs.dbflow5.sql.language

import com.raizlabs.dbflow5.BaseUnitTest
import com.raizlabs.dbflow5.assertEquals
import com.raizlabs.dbflow5.config.databaseForTable
import com.raizlabs.dbflow5.models.SimpleModel
import com.raizlabs.dbflow5.query.UnSafeStringOperator
import com.raizlabs.dbflow5.query.select
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