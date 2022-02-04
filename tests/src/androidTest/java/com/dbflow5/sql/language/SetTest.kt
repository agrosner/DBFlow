package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.assertEquals
import com.dbflow5.config.database
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.models.TwoColumnModel_Table
import com.dbflow5.query.set
import com.dbflow5.query.update
import org.junit.Test

class SetTest : BaseUnitTest() {

    @Test
    fun validateSetWithConditions() {
        database<TestDatabase> { db ->
            "UPDATE `SimpleModel` SET `name`='name'".assertEquals(
                update(db.simpleModelAdapter) set SimpleModel_Table.name.`is`(
                    "name"
                )
            )
        }
    }

    @Test
    fun validateMultipleConditions() {
        database<TestDatabase> { db ->
            "UPDATE `TwoColumnModel` SET `name`='name', `id`=0".assertEquals(
                update(db.twoColumnModelAdapter) set TwoColumnModel_Table.name.eq(
                    "name"
                ) and TwoColumnModel_Table.id.eq(0)
            )
        }
    }
}