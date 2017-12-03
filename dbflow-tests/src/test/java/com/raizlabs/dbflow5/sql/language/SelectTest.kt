package com.raizlabs.dbflow5.sql.language

import com.raizlabs.dbflow5.BaseUnitTest
import com.raizlabs.dbflow5.assertEquals
import com.raizlabs.dbflow5.config.databaseForTable
import com.raizlabs.dbflow5.models.SimpleModel
import com.raizlabs.dbflow5.models.TwoColumnModel
import com.raizlabs.dbflow5.models.TwoColumnModel_Table.id
import com.raizlabs.dbflow5.models.TwoColumnModel_Table.name
import com.raizlabs.dbflow5.query.select
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