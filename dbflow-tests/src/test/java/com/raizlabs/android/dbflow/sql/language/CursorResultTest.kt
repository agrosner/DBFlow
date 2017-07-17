package com.raizlabs.android.dbflow.sql.language

import android.database.StaleDataException
import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.kotlinextensions.cursorResult
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.save
import com.raizlabs.android.dbflow.kotlinextensions.select
import com.raizlabs.android.dbflow.kotlinextensions.toCustomList
import com.raizlabs.android.dbflow.kotlinextensions.toCustomListClose
import com.raizlabs.android.dbflow.kotlinextensions.toCustomModel
import com.raizlabs.android.dbflow.kotlinextensions.toCustomModelClose
import com.raizlabs.android.dbflow.models.SimpleCustomModel
import com.raizlabs.android.dbflow.models.SimpleModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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

    @Test
    fun validateToListClose() {
        val list = result.toListClose()
        assertEquals(10, list.size)
        var exception = false
        try {
            result.toListClose()
        } catch (i: Exception) {
            when (i) {
                is IllegalStateException, is StaleDataException -> exception = true
                else -> throw i
            }
        }
        assertTrue(exception)
    }

    @Test
    fun validateToCustomList() {
        val list = result.toCustomList<SimpleCustomModel>()
        assertEquals(10, list.size)
    }

    @Test
    fun validateToCustomListClose() {
        val list = result.toCustomListClose<SimpleCustomModel>()
        assertEquals(10, list.size)
        var exception = false
        try {
            result.toCustomListClose<SimpleCustomModel>()
        } catch (i: Exception) {
            when (i) {
                is IllegalStateException, is StaleDataException -> exception = true
                else -> throw i
            }
        }
        assertTrue(exception)
    }

    @Test
    fun validateToModel() {
        val model = result.toModel()
        assertNotNull(model)
    }

    @Test
    fun validateToModelClose() {
        val model = result.toModelClose()
        assertNotNull(model)
        var exception = false
        try {
            result.toModelClose()
        } catch (i: Exception) {
            when (i) {
                is IllegalStateException, is StaleDataException -> exception = true
                else -> throw i
            }
        }
        assertTrue(exception)
    }

    @Test
    fun validateToCustomModel() {
        val model = result.toCustomModel<SimpleCustomModel>()
        assertNotNull(model)
    }

    @Test
    fun validateToCustomModelClose() {
        val model = result.toCustomModelClose<SimpleCustomModel>()
        assertNotNull(model)
        var exception = false
        try {
            result.toCustomModelClose<SimpleCustomModel>()
        } catch (i: Exception) {
            when (i) {
                is IllegalStateException, is StaleDataException -> exception = true
                else -> throw i
            }
        }
        assertTrue(exception)
    }

    @Test
    fun validateNullCursor() {
        result.swapCursor(null)

        assertTrue(result.toList().isEmpty())
        assertTrue(result.toCustomList<String>().isEmpty())
        assertTrue(result.toModel() == null)
        assertTrue(result.toCustomModel<String>() == null)
    }
}