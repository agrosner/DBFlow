package com.dbflow5.database.transaction

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.coroutines.awaitInsert
import com.dbflow5.coroutines.awaitSave
import com.dbflow5.coroutines.awaitUpdate
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.TwoColumnModel
import com.dbflow5.query.select
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.util.*

class FastStoreModelTransactionTest : BaseUnitTest() {

    @Test
    fun testSaveBuilder() {
        runBlocking {
            database(TestDatabase::class) { db ->
                val result = (0..9)
                        .map { SimpleModel("$it") }.awaitSave(db)
                val list = (select from SimpleModel::class).queryList(db)
                assertEquals(10, list.size)
                assertEquals(10L, result)
            }
        }
    }

    @Test
    fun testInsertBuilder() {
        runBlocking {
            database(TestDatabase::class) { db ->
                val result = (0..9)
                        .map { SimpleModel("$it") }.awaitInsert(db)
                val list = (select from SimpleModel::class).queryList(db)
                assertEquals(10, list.size)
                assertEquals(10L, result)
            }
        }
    }

    @Test
    fun testUpdateBuilder() {
        runBlocking {
            database(TestDatabase::class) { db ->
                val oldList = (0..9).map { TwoColumnModel("$it", Random().nextInt()) }
                oldList.awaitInsert(db)

                (0..9).map { TwoColumnModel("$it", Random().nextInt()) }.awaitUpdate(db)

                val list = (select from TwoColumnModel::class).queryList(db)
                assertEquals(10, list.size)
                list.forEachIndexed { index, model ->
                    assertNotEquals(model, oldList[index])
                }
            }
        }
    }
}