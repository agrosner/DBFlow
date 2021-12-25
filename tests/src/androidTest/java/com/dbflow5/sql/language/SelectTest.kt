package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.assertEquals
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.TwoColumnModel
import com.dbflow5.models.TwoColumnModel_Table
import com.dbflow5.query.select
import org.junit.Test

class SelectTest : BaseUnitTest() {

    @Test
    fun validateSelect() {
        "SELECT `name`,`id` FROM `TwoColumnModel`".assertEquals(
            select(
                TwoColumnModel_Table.name,
                TwoColumnModel_Table.id
            ) from TwoColumnModel::class
        )
    }

    @Test
    fun validateSelectDistinct() {
        "SELECT DISTINCT `name` FROM `SimpleModel`".assertEquals(
            select(TwoColumnModel_Table.name).distinct() from SimpleModel::class
        )
    }
}