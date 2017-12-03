package com.raizlabs.dbflow5.dbflow.sql.language

import com.raizlabs.dbflow5.dbflow.BaseUnitTest
import com.raizlabs.dbflow5.config.databaseForTable
import com.raizlabs.dbflow5.dbflow.models.SimpleModel
import com.raizlabs.dbflow5.dbflow.models.SimpleModel_Table
import com.raizlabs.dbflow5.query.ExistenceOperator
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