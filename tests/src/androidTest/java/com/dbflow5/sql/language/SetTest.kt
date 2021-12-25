package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.assertEquals
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.models.TwoColumnModel
import com.dbflow5.models.TwoColumnModel_Table
import com.dbflow5.query.set
import com.dbflow5.query.update
import org.junit.Test

class SetTest : BaseUnitTest() {

    @Test
    fun validateSetWithConditions() {
        "UPDATE `SimpleModel` SET `name`='name'".assertEquals(
            update<SimpleModel>() set SimpleModel_Table.name.`is`(
                "name"
            )
        )
    }

    @Test
    fun validateMultipleConditions() {
        "UPDATE `TwoColumnModel` SET `name`='name', `id`=0".assertEquals(
            update<TwoColumnModel>() set TwoColumnModel_Table.name.eq(
                "name"
            ) and TwoColumnModel_Table.id.eq(0)
        )
    }
}