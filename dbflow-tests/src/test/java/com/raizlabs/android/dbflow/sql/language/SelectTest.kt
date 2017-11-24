package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.assertEquals
import com.raizlabs.android.dbflow.config.writableDatabaseForTable
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.models.TwoColumnModel
import com.raizlabs.android.dbflow.models.TwoColumnModel_Table.id
import com.raizlabs.android.dbflow.models.TwoColumnModel_Table.name
import org.junit.Test

class SelectTest : BaseUnitTest() {

    @Test
    fun validateSelect() = writableDatabaseForTable<TwoColumnModel> {
        assertEquals("SELECT `name`,`id` FROM `TwoColumnModel`",
                select(name, id) from TwoColumnModel::class)
    }

    @Test
    fun validateSelectDistinct() = writableDatabaseForTable<SimpleModel> {
        assertEquals("SELECT DISTINCT `name` FROM `SimpleModel`",
                select(name).distinct() from SimpleModel::class)
    }
}