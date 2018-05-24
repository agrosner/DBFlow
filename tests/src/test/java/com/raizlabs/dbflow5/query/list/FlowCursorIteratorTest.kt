package com.raizlabs.dbflow5.query.list

import com.raizlabs.dbflow5.BaseUnitTest
import com.raizlabs.dbflow5.config.databaseForTable
import com.raizlabs.dbflow5.models.SimpleModel
import com.raizlabs.dbflow5.query.select
import com.raizlabs.dbflow5.structure.save
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Description:
 */
class FlowCursorIteratorTest : BaseUnitTest() {


    @Test
    fun testCanIterateFullList() {
        var count = 0
        databaseForTable<SimpleModel>().beginTransactionAsync { db ->
            (0..9).forEach {
                SimpleModel("$it").save(db)
            }
            (select from SimpleModel::class).cursorList(db).iterator()
        }.success { _, iterator ->
            assertFalse(iterator.isClosed)
            iterator.use { cursorIterator ->
                cursorIterator.forEach {
                    assertEquals("$count", it.name)
                    count++
                }
            }
            assertTrue(iterator.isClosed)
        }.execute()
    }

    @Test
    fun testCanIteratePartialList() {
        databaseForTable<SimpleModel>().beginTransactionAsync { db ->
            (0..9).forEach {
                SimpleModel("$it").save(db)
            }

            (select from SimpleModel::class).cursorList(db)
                .iterator(2, 7)
        }.success { _, iterator ->
            var count = 0
            iterator.forEach {
                assertEquals("${count + 2}", it.name)
                count++
            }
            assertEquals(7, count)
        }.execute()
    }

    @Test
    fun testCanSupplyBadMaximumValue() {
        databaseForTable<SimpleModel>().beginTransactionAsync { db ->
            (0..9).forEach {
                SimpleModel("$it").save()
            }

            (select from SimpleModel::class).cursorList(db)
                .iterator(2, Long.MAX_VALUE)
        }.success { _, iterator ->
            var count = 0
            iterator.forEach {
                assertEquals("${count + 2}", it.name)
                count++
            }
            assertEquals(8, count)
        }.execute()
    }
}