package com.raizlabs.android.dbflow.database.transaction

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.config.database
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.models.TwoColumnModel
import com.raizlabs.android.dbflow.query.list
import com.raizlabs.android.dbflow.query.select
import com.raizlabs.dbflow.coroutines.awaitInsert
import com.raizlabs.dbflow.coroutines.awaitSave
import com.raizlabs.dbflow.coroutines.awaitUpdate
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.util.*

class FastStoreModelTransactionTest : BaseUnitTest() {

    @Test
    fun testSaveBuilder() {
        runBlocking {
            database(TestDatabase::class) {
                val result = (0..9)
                        .map { SimpleModel("$it") }.awaitSave(this)
                val list = (writableDatabase.select from SimpleModel::class).list
                assertEquals(10, list.size)
                assertEquals(10L, result)
            }
        }
    }

    @Test
    fun testInsertBuilder() {
        runBlocking {
            database(TestDatabase::class) {
                val result = (0..9)
                        .map { SimpleModel("$it") }.awaitInsert(this)
                val list = (writableDatabase.select from SimpleModel::class).list
                assertEquals(10, list.size)
                assertEquals(10L, result)
            }
        }
    }

    @Test
    fun testUpdateBuilder() {
        runBlocking {
            database(TestDatabase::class) {
                val oldList = (0..9).map { TwoColumnModel("$it", Random().nextInt()) }
                oldList.awaitInsert(this)

                (0..9).map { TwoColumnModel("$it", Random().nextInt()) }.awaitUpdate(this)

                val list = (writableDatabase.select from TwoColumnModel::class).list
                assertEquals(10, list.size)
                list.forEachIndexed { index, model ->
                    assertNotEquals(model, oldList[index])
                }
            }
        }
    }
}