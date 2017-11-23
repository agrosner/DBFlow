package com.raizlabs.android.dbflow.database.transaction

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.kotlinextensions.database
import com.raizlabs.android.dbflow.kotlinextensions.fastInsert
import com.raizlabs.android.dbflow.kotlinextensions.fastSave
import com.raizlabs.android.dbflow.kotlinextensions.fastUpdate
import com.raizlabs.android.dbflow.sql.language.from
import com.raizlabs.android.dbflow.kotlinextensions.list
import com.raizlabs.android.dbflow.sql.language.select
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.models.TwoColumnModel
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