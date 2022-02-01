package com.dbflow5.models

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.config.writableTransaction
import com.dbflow5.oneToManyBaseModel
import com.dbflow5.oneToManyModel
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

class OneToManyModelTest : BaseUnitTest() {

    @Test
    fun testOneToManyModel() = runBlockingTest {
        database<TestDatabase>().writableTransaction {
            oneToManyModel.save(
                OneToManyModel(
                    name = "name"
                )
            )

            oneToManyBaseModel.save(
                OneToManyBaseModel(
                    id = 1,
                    parentName = "name"
                )
            )

            /*val items =
                (select from OneToManyModel::class).requireCustomSingle<OneToManyModel_OneToManyBaseModel>(
                    db
                )
            assertEquals(items.children.size, 1)*/
        }
    }
}