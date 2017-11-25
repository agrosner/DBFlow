package com.raizlabs.android.dbflow.list

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.config.databaseForTable
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.sql.language.select
import com.raizlabs.android.dbflow.structure.save
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Description:
 */
class FlowCursorIteratorTest : BaseUnitTest() {


    @Test
    fun testCanIterateFullList() {
        databaseForTable<SimpleModel> {
            (0..9).forEach {
                SimpleModel("$it").save()
            }


            var count = 0
            (select from SimpleModel::class).cursorList().iterator().forEach {
                assertEquals("$count", it.name)
                count++
            }
        }
    }

    @Test
    fun testCanIteratePartialList() {
        databaseForTable<SimpleModel> {
            (0..9).forEach {
                SimpleModel("$it").save()
            }

            var count = 2
            (select from SimpleModel::class).cursorList().iterator(2, 7).forEach {
                assertEquals("$count", it.name)
                count++
            }
            assertEquals(7, count)
        }
    }

    @Test
    fun testCanSupplyBadMaximumValue() {
        databaseForTable<SimpleModel> {
            (0..9).forEach {
                SimpleModel("$it").save()
            }

            var count = 2
            (select from SimpleModel::class).cursorList().iterator(2, Long.MAX_VALUE).forEach {
                assertEquals("$count", it.name)
                count++
            }
            assertEquals(8, count)
        }
    }
}