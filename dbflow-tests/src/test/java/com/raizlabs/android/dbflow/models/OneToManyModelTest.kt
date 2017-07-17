package com.raizlabs.android.dbflow.models

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.kotlinextensions.delete
import com.raizlabs.android.dbflow.kotlinextensions.exists
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.list
import com.raizlabs.android.dbflow.kotlinextensions.result
import com.raizlabs.android.dbflow.kotlinextensions.save
import com.raizlabs.android.dbflow.kotlinextensions.select
import com.raizlabs.android.dbflow.kotlinextensions.writableDatabaseForTable
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OneToManyModelTest : BaseUnitTest() {

    @Test
    fun testOneToManyModel() {

        var testModel2 = TwoColumnModel("Greater", 4)
        testModel2.save()

        testModel2 = TwoColumnModel("Lesser", 1)
        testModel2.save()

        // assert we save
        var oneToManyModel = OneToManyModel("HasOrders")
        oneToManyModel.save()
        assertTrue(oneToManyModel.exists())

        // assert loading works as expected.
        oneToManyModel = (select from OneToManyModel::class).result!!
        assertNotNull(oneToManyModel.getRelatedOrders())
        assertTrue(!oneToManyModel.getRelatedOrders().isEmpty())

        // assert the deletion cleared the variable
        oneToManyModel.delete()
        assertFalse(oneToManyModel.exists())
        assertNull(oneToManyModel.orders)

        // assert singular relationship was deleted.
        val list = (select from TwoColumnModel::class).list
        assertTrue(list.size == 1)

    }
}