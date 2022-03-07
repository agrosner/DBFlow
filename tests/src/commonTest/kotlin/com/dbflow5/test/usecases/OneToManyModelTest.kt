package com.dbflow5.test.usecases

import com.dbflow5.query.select
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.test.OneToManyBaseModel
import com.dbflow5.test.OneToManyModel
import com.dbflow5.test.TestDatabase_Database
import kotlin.test.Test
import kotlin.test.assertEquals

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

        // TODO: how to query for one to many generator?
/*
        val items =
            (select from oneToManyModelOneToManyBaseModelQueryAdapter).single()
        assertEquals(items.children.size, 1)*/
    }
}