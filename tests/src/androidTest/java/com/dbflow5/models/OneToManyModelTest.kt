package com.dbflow5.models

import com.dbflow5.TestDatabase_Database
import com.dbflow5.oneToManyBaseModelAdapter
import com.dbflow5.oneToManyModelAdapter
import com.dbflow5.test.DatabaseTestRule
import org.junit.Rule
import kotlin.test.Test

class OneToManyModelTest {

    
    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun testOneToManyModel() = dbRule.runTest {
        oneToManyModelAdapter.save(
            OneToManyModel(
                name = "name"
            )
        )

        oneToManyBaseModelAdapter.save(
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