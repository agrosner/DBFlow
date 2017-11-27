package com.raizlabs.android.dbflow.database.transaction

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.config.database
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.models.TwoColumnModel
import com.raizlabs.android.dbflow.query.list
import com.raizlabs.android.dbflow.query.select
import com.raizlabs.android.dbflow.transaction.fastInsert
import com.raizlabs.android.dbflow.transaction.fastSave
import com.raizlabs.android.dbflow.transaction.fastUpdate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.util.*

class FastStoreModelTransactionTest : BaseUnitTest() {

    @Test
    fun testSaveBuilder() {

        database(TestDatabase::class) {
            beginTransactionAsync((0..9)
                    .map { SimpleModel("$it") }
                    .fastSave().build())
                    .execute()

            val list = (writableDatabase.select from SimpleModel::class).list
            assertEquals(10, list.size)
        }
    }

    @Test
    fun testInsertBuilder() {
        database(TestDatabase::class) {
            beginTransactionAsync((0..9)
                    .map { SimpleModel("$it") }
                    .fastInsert().build())
                    .execute()

            val list = (writableDatabase.select from SimpleModel::class).list
            assertEquals(10, list.size)
        }
    }

    @Test
    fun testUpdateBuilder() {
        database(TestDatabase::class) {
            val oldList = (0..9).map { TwoColumnModel("$it", Random().nextInt()) }
            beginTransactionAsync(oldList.fastInsert().build())
                    .execute()

            beginTransactionAsync((0..9).map { TwoColumnModel("$it", Random().nextInt()) }
                    .fastUpdate().build())
                    .execute()

            val list = (writableDatabase.select from TwoColumnModel::class).list
            assertEquals(10, list.size)
            list.forEachIndexed { index, model ->
                assertNotEquals(model, oldList[index])
            }
        }
    }
}