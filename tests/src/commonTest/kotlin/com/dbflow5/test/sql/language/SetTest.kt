package com.dbflow5.test.sql.language

import com.dbflow5.test.TestDatabase_Database
import com.dbflow5.test.assertEquals
import com.dbflow5.test.SimpleModel_Table
import com.dbflow5.test.TwoColumnModel_Table
import com.dbflow5.query.update
import com.dbflow5.test.simpleModelAdapter
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.test.twoColumnModelAdapter
import org.junit.Rule
import kotlin.test.Test

class SetTest {

    
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