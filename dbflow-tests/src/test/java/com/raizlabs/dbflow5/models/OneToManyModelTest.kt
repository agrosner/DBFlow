package com.raizlabs.dbflow5.models

import com.raizlabs.dbflow5.BaseUnitTest
import com.raizlabs.dbflow5.TestDatabase
import com.raizlabs.dbflow5.config.database
import com.raizlabs.dbflow5.query.list
import com.raizlabs.dbflow5.query.result
import com.raizlabs.dbflow5.query.select
import com.raizlabs.dbflow5.structure.delete
import com.raizlabs.dbflow5.structure.exists
import com.raizlabs.dbflow5.structure.save
import org.junit.Assert.*
import org.junit.Test

class OneToManyModelTest : BaseUnitTest() {

    @Test
    fun testOneToManyModel() {
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