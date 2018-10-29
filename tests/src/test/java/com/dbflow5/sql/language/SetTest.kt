package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.assertEquals
import com.dbflow5.config.databaseForTable
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.SimpleModel_Table.name
import com.dbflow5.models.TwoColumnModel_Table.id
import com.dbflow5.query.set
import com.dbflow5.query.update
import org.junit.Test

class SetTest : BaseUnitTest() {

    @Test
    fun validateSetWithConditions() {
        databaseForTable<SimpleModel> { dbFlowDatabase ->
            "UPDATE `SimpleModel` SET `name`='name'".assertEquals(update<SimpleModel>() set name.`is`("name"))
        }
    }

    @Test
    fun validateMultipleConditions() {
        databaseForTable<SimpleModel> { dbFlowDatabase ->
            "UPDATE `SimpleModel` SET `name`='name', `id`=0".assertEquals(update<SimpleModel>() set name.eq("name") and id.eq(0))
        }
    }
}