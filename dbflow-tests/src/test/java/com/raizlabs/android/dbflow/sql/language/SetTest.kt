package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.assertEquals
import com.raizlabs.android.dbflow.config.writableDatabaseForTable
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.models.SimpleModel_Table.name
import com.raizlabs.android.dbflow.models.TwoColumnModel_Table.id
import org.junit.Test

class SetTest : BaseUnitTest() {

    @Test
    fun validateSetWithConditions() = writableDatabaseForTable<SimpleModel> {
        assertEquals("UPDATE `SimpleModel` SET `name`='name'",
                update<SimpleModel>() set name.`is`("name"))
    }

    @Test
    fun validateMultipleConditions() = writableDatabaseForTable<SimpleModel> {
        assertEquals("SET `name`='name', `id`=0",
                update<SimpleModel>() set name.eq("name") and id.eq(0))
    }
}