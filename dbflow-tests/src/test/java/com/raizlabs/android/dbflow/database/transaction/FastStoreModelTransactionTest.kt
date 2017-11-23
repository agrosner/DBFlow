package com.raizlabs.android.dbflow.database.transaction

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.config.database
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.models.TwoColumnModel
import com.raizlabs.android.dbflow.sql.language.select
import com.raizlabs.android.dbflow.sql.queriable.list
import com.raizlabs.android.dbflow.structure.database.transaction.fastInsert
import com.raizlabs.android.dbflow.structure.database.transaction.fastSave
import com.raizlabs.android.dbflow.structure.database.transaction.fastUpdate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.util.*

class FastStoreModelTransactionTest : BaseUnitTest() {

    @Test
    fun testSaveBuilder() {

        database<TestDatabase>()
                .beginTransactionAsync((0..9)
                        .map { SimpleModel("$it") }
                        .fastSave().build())
                .execute()

        val list = (select from SimpleModel::class).list
        assertEquals(10, list.size)
    }

    @Test
    fun testInsertBuilder() {

        database<TestDatabase>()
                .beginTransactionAsync((0..9)
                        .map { SimpleModel("$it") }
                        .fastInsert().build())
                .execute()

        val list = (select from SimpleModel::class).list
        assertEquals(10, list.size)
    }

    @Test
    fun testUpdateBuilder() {

        val oldList = (0..9).map { TwoColumnModel("$it", Random().nextInt()) }
        database<TestDatabase>()
                .beginTransactionAsync(oldList.fastInsert().build())
                .execute()

        database<TestDatabase>()
                .beginTransactionAsync((0..9).map { TwoColumnModel("$it", Random().nextInt()) }
                        .fastUpdate().build())
                .execute()

        val list = (select from TwoColumnModel::class).list
        assertEquals(10, list.size)
        list.forEachIndexed { index, model ->
            assertNotEquals(model, oldList[index])
        }
    }
}