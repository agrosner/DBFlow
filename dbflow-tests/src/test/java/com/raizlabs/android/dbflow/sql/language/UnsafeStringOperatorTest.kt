package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.assertEquals
import com.raizlabs.android.dbflow.models.SimpleModel
import org.junit.Test

class UnsafeStringOperatorTest : BaseUnitTest() {

    @Test
    fun testCanIncludeInQuery() {
        val op = UnSafeStringOperator("name = ?, id = ?, test = ?", arrayOf("'name'", "0", "'test'"))
        assertEquals("name = 'name', id = 0, test = 'test'", op)
        assertEquals("SELECT * FROM `SimpleModel` WHERE name = 'name', id = 0, test = 'test'",
                select from SimpleModel::class where op)
    }
}