package com.dbflow5.query.list

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.beginTransactionAsync
import com.dbflow5.config.database
import com.dbflow5.models.SimpleModel
import com.dbflow5.query.select
import com.dbflow5.simpleModelAdapter
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
        database<TestDatabase>().beginTransactionAsync {
            (0..9).forEach {
                simpleModelAdapter.save(SimpleModel("$it"))
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
        }.enqueue()
    }

    @Test
    fun testCanIteratePartialList() {
        database<TestDatabase>().beginTransactionAsync {
            (0..9).forEach {
                simpleModelAdapter.save(SimpleModel("$it"))
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
        }.enqueue()
    }

    @Test
    fun testCanSupplyBadMaximumValue() {
        database<TestDatabase>().beginTransactionAsync {
            (0..9).forEach {
                simpleModelAdapter.save(SimpleModel("$it"))
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
        }.enqueue()
    }
}