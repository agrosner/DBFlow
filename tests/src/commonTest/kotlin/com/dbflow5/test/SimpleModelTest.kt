package com.dbflow5.test

import kotlin.test.Test

class SimpleModelTest {

    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun canSaveSimpleModel() = dbRule.runTest {
        val model = SimpleModel(
            name = "5"
        )
        simpleModelAdapter.save(model)
    }
}
