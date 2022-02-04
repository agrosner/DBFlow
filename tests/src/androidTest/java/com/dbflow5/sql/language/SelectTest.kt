package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.assertEquals
import com.dbflow5.config.database
import com.dbflow5.models.TwoColumnModel_Table
import com.dbflow5.query.select
import org.junit.Test

class SelectTest : BaseUnitTest() {

    @Test
    fun validateSelect() {
        database<TestDatabase> { db ->
            "SELECT `name`,`id` FROM `TwoColumnModel`".assertEquals(
                select(
                    TwoColumnModel_Table.name,
                    TwoColumnModel_Table.id
                ) from db.twoColumnModelAdapter
            )
        }
    }

    @Test
    fun validateSelectDistinct() {
        database<TestDatabase> { db ->
            "SELECT DISTINCT `name` FROM `SimpleModel`".assertEquals(
                select(TwoColumnModel_Table.name).distinct() from db.simpleModelAdapter
            )
        }
    }
}