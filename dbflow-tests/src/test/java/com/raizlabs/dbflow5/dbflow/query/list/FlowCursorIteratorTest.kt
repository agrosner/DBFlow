package com.raizlabs.dbflow5.dbflow.query.list

import com.raizlabs.dbflow5.dbflow.BaseUnitTest
import com.raizlabs.dbflow5.config.databaseForTable
import com.raizlabs.dbflow5.dbflow.models.SimpleModel
import com.raizlabs.dbflow5.structure.save
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