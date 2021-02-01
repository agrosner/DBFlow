package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.assertEquals
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.TwoColumnModel
import com.dbflow5.models.TwoColumnModel_Table.id
import com.dbflow5.models.TwoColumnModel_Table.name
import com.dbflow5.query.select
import org.junit.Test

class SelectTest : BaseUnitTest() {

    @Test
    fun validateSelect() {
        "SELECT `name`,`id` FROM `TwoColumnModel`".assertEquals(select(name, id) from TwoColumnModel::class)
    }

    @Test
    fun validateSelectDistinct() {
        "SELECT DISTINCT `name` FROM `SimpleModel`".assertEquals(select(name).distinct() from SimpleModel::class)
    }
}