package com.raizlabs.dbflow5.sql.language

import com.raizlabs.dbflow5.BaseUnitTest
import com.raizlabs.dbflow5.assertEquals
import com.raizlabs.dbflow5.config.databaseForTable
import com.raizlabs.dbflow5.models.SimpleModel
import com.raizlabs.dbflow5.models.SimpleModel_Table.name
import com.raizlabs.dbflow5.models.TwoColumnModel_Table.id
import com.raizlabs.dbflow5.query.set
import com.raizlabs.dbflow5.query.update
import org.junit.Test

class SetTest : BaseUnitTest() {

    @Test
    fun validateSetWithConditions() {
        databaseForTable<SimpleModel> {
            assertEquals("UPDATE `SimpleModel` SET `name`='name'",
                    update<SimpleModel>() set name.`is`("name"))
        }
    }

    @Test
    fun validateMultipleConditions() {
        databaseForTable<SimpleModel> {
            assertEquals("UPDATE `SimpleModel` SET `name`='name', `id`=0",
                    update<SimpleModel>() set name.eq("name") and id.eq(0))
        }
    }
}