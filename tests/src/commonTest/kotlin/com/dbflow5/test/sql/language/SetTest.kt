package com.dbflow5.test.sql.language

import com.dbflow5.query.update
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.test.SimpleModel
import com.dbflow5.test.TestDatabase_Database
import com.dbflow5.test.TestRule
import com.dbflow5.test.TwoColumnModel
import com.dbflow5.test.assertEquals
import kotlin.test.Test

class SetTest : TestRule() {


    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun validateSetWithConditions() {
        dbRule {
            "UPDATE `SimpleModel` SET `name` = 'name'".assertEquals(
                simpleModelAdapter.update() set SimpleModel.name.eq(
                    "name"
                )
            )
        }
    }

    @Test
    fun validateMultipleConditions() {
        dbRule {
            "UPDATE `TwoColumnModel` SET `name` = 'name', `id` = 0".assertEquals(
                twoColumnModelAdapter.update() set TwoColumnModel.name.eq(
                    "name"
                ) and TwoColumnModel.id.eq(0)
            )
        }
    }
}