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
            "name = 'name', id = 0, test = 'test'".assertEquals(op)
            "SELECT * FROM `SimpleModel` WHERE name = 'name', id = 0, test = 'test'".assertEquals(select from SimpleModel::class where op)
        }
    }
}