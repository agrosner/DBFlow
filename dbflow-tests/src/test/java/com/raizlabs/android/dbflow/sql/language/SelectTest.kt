package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.assertEquals
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.models.TwoColumnModel
import com.raizlabs.android.dbflow.models.TwoColumnModel_Table.id
import com.raizlabs.android.dbflow.models.TwoColumnModel_Table.name
import com.raizlabs.android.dbflow.sql.language.SQLite.select
import org.junit.Test

class SelectTest : BaseUnitTest() {

    @Test
    fun validateSelect() {
        assertEquals("SELECT `name`,`id` FROM `TwoColumnModel`", select(name, id) from TwoColumnModel::class)
    }

    @Test
    fun validateSelectDistinct() {
        assertEquals("SELECT DISTINCT `name` FROM `SimpleModel`", select(name).distinct() from SimpleModel::class)
    }
}