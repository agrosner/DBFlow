package com.raizlabs.android.dbflow.models

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.config.database
import com.raizlabs.android.dbflow.query.select
import com.raizlabs.android.dbflow.query.list
import com.raizlabs.android.dbflow.query.result
import com.raizlabs.android.dbflow.structure.delete
import com.raizlabs.android.dbflow.structure.exists
import com.raizlabs.android.dbflow.structure.save
import org.junit.Assert.*
import org.junit.Test

class OneToManyModelTest : BaseUnitTest() {

    @Test
    fun testOneToManyModel(){
        database(TestDatabase::class) {
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
            assertNotNull(oneToManyModel.getRelatedOrders(this))
            assertTrue(!oneToManyModel.getRelatedOrders(this).isEmpty())

            // assert the deletion cleared the variable
            oneToManyModel.delete()
            assertFalse(oneToManyModel.exists())
            assertNull(oneToManyModel.orders)

            // assert singular relationship was deleted.
            val list = (select from TwoColumnModel::class).list
            assertTrue(list.size == 1)
        }
    }
}