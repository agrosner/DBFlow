package com.raizlabs.dbflow5.dbflow.sql.language

import com.raizlabs.dbflow5.dbflow.BaseUnitTest
import com.raizlabs.dbflow5.dbflow.assertEquals
import com.raizlabs.dbflow5.config.databaseForTable
import com.raizlabs.dbflow5.dbflow.models.SimpleModel
import com.raizlabs.dbflow5.dbflow.models.TwoColumnModel
import com.raizlabs.dbflow5.dbflow.models.TwoColumnModel_Table.id
import com.raizlabs.dbflow5.dbflow.models.TwoColumnModel_Table.name
import org.junit.Test

class SelectTest : BaseUnitTest() {

    @Test
    fun validateSelect() {
        databaseForTable<TwoColumnModel> {
            assertEquals("SELECT `name`,`id` FROM `TwoColumnModel`",
                    select(name, id) from TwoColumnModel::class)
        }
    }

    @Test
    fun validateSelectDistinct() {
        databaseForTable<SimpleModel> {
            assertEquals("SELECT DISTINCT `name` FROM `SimpleModel`",
                    select(name).distinct() from SimpleModel::class)
        }
    }
}