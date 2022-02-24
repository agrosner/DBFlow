package com.dbflow5.sql.language

import com.dbflow5.TestDatabase_Database
import com.dbflow5.assertEquals
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.models.TwoColumnModel_Table
import com.dbflow5.query.update
import com.dbflow5.simpleModelAdapter
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.twoColumnModelAdapter
import org.junit.Rule
import org.junit.Test

class SetTest {

    @get:Rule
    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun validateSetWithConditions() {
        dbRule {
            "UPDATE `SimpleModel` SET `name` = 'name'".assertEquals(
                simpleModelAdapter.update() set SimpleModel_Table.name.eq(
                    "name"
                )
            )
        }
    }

    @Test
    fun validateMultipleConditions() {
        dbRule {
            "UPDATE `TwoColumnModel` SET `name` = 'name', `id` = 0".assertEquals(
                twoColumnModelAdapter.update() set TwoColumnModel_Table.name.eq(
                    "name"
                ) and TwoColumnModel_Table.id.eq(0)
            )
        }
    }
}