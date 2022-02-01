package com.dbflow5.database.transaction

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.config.writableTransaction
import com.dbflow5.coroutines.awaitInsert
import com.dbflow5.coroutines.awaitSave
import com.dbflow5.coroutines.awaitUpdate
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.TwoColumnModel
import com.dbflow5.query.select
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.util.Random

class FastStoreModelTransactionTest : BaseUnitTest() {

    @Test
    fun testSaveBuilder() = runBlockingTest {
        database<TestDatabase>().writableTransaction {
            val result = (0..9)
                .map { SimpleModel("$it") }.awaitSave(db)
            val list = (select from SimpleModel::class).queryList()
            assertEquals(10, list.size)
            assertEquals(10, result.getOrThrow().size)
        }
    }

    @Test
    fun testInsertBuilder() = runBlockingTest {
        database<TestDatabase>().writableTransaction {
            val result = (0..9)
                .map { SimpleModel("$it") }.awaitInsert(db)
            val list = (select from SimpleModel::class).queryList()
            assertEquals(10, list.size)
            assertEquals(10, result.getOrThrow().size)
        }
    }

    @Test
    fun testUpdateBuilder() = runBlockingTest {
        database<TestDatabase>().writableTransaction {
            val oldList = (0..9).map { TwoColumnModel("$it", Random().nextInt()) }
            oldList.awaitInsert(db)

            (0..9).map { TwoColumnModel("$it", Random().nextInt()) }.awaitUpdate(db)

            val list = (select from TwoColumnModel::class).queryList()
            assertEquals(10, list.size)
            list.forEachIndexed { index, model ->
                assertNotEquals(model, oldList[index])
            }
        }
    }
}