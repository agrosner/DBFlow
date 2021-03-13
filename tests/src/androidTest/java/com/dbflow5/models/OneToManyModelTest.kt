package com.dbflow5.models

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.query.select
import com.dbflow5.structure.delete
import com.dbflow5.structure.exists
import com.dbflow5.structure.save
import org.junit.Assert.*
import org.junit.Test

class OneToManyModelTest : BaseUnitTest() {

    @Test
    fun testOneToManyModel() {
        database(TestDatabase::class) { db ->
            var testModel2 = TwoColumnModel("Greater", 4)
            testModel2.save(db)

            testModel2 = TwoColumnModel("Lesser", 1)
            testModel2.save(db)

            // assert we save
            var oneToManyModel = OneToManyModel("HasOrders")
            oneToManyModel.save(db)
            assertTrue(oneToManyModel.exists(db))

            // assert loading works as expected.
            oneToManyModel = (select from OneToManyModel::class).requireSingle(db)
            assertNotNull(oneToManyModel.getRelatedOrders(db))
            assertTrue(!oneToManyModel.getRelatedOrders(db).isEmpty())

            // assert the deletion cleared the variable
            oneToManyModel.delete(db)
            assertFalse(oneToManyModel.exists(db))
            assertNull(oneToManyModel.orders)

            // assert singular relationship was deleted.
            val list = (select from TwoColumnModel::class).queryList(db)
            assertTrue(list.size == 1)
        }
    }
}