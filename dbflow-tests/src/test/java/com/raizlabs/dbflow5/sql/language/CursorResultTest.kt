package com.raizlabs.dbflow5.sql.language

import android.database.StaleDataException
import com.raizlabs.dbflow5.BaseUnitTest
import com.raizlabs.dbflow5.config.databaseForTable
import com.raizlabs.dbflow5.models.SimpleCustomModel
import com.raizlabs.dbflow5.models.SimpleModel
import com.raizlabs.dbflow5.query.CursorResult
import com.raizlabs.dbflow5.query.cursorResult
import com.raizlabs.dbflow5.query.select
import com.raizlabs.dbflow5.query.toCustomList
import com.raizlabs.dbflow5.query.toCustomListClose
import com.raizlabs.dbflow5.query.toCustomModel
import com.raizlabs.dbflow5.query.toCustomModelClose
import com.raizlabs.dbflow5.structure.save
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CursorResultTest : BaseUnitTest() {

    lateinit var result: CursorResult<SimpleModel>

    @Before
    fun prepareList() {
        databaseForTable<SimpleModel> {
            (0..9).forEach { SimpleModel("$it").save() }
            result = (select from SimpleModel::class).cursorResult
        }
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