package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.SimpleModel
import com.raizlabs.android.dbflow.kotlinextensions.cursorResult
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.save
import com.raizlabs.android.dbflow.kotlinextensions.select
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CursorResultTest : BaseUnitTest() {

    lateinit var result: CursorResult<SimpleModel>

    @Before
    fun prepareList() {
        (0..9).forEach { SimpleModel("$it").save() }
        result = (select from SimpleModel::class).cursorResult
    }

    @Test
    fun validateToList() {
        val list = result.toList()
        assertEquals(10, list.size)
    }
}