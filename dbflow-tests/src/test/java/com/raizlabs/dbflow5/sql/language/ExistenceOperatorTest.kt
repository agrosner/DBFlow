package com.raizlabs.dbflow5.sql.language

import com.raizlabs.dbflow5.BaseUnitTest
import com.raizlabs.dbflow5.config.databaseForTable
import com.raizlabs.dbflow5.models.SimpleModel
import com.raizlabs.dbflow5.models.SimpleModel_Table
import com.raizlabs.dbflow5.query.ExistenceOperator
import com.raizlabs.dbflow5.query.select
import org.junit.Assert.assertEquals
import org.junit.Test

class ExistenceOperatorTest : BaseUnitTest() {


    @Test
    fun validateQuery() {
        databaseForTable<SimpleModel> {
            assertEquals("EXISTS (SELECT * FROM `SimpleModel` WHERE `name`='name')",
                    ExistenceOperator(
                            (select from SimpleModel::class
                                    where SimpleModel_Table.name.eq("name")))
                            .query.trim())
        }
    }
}