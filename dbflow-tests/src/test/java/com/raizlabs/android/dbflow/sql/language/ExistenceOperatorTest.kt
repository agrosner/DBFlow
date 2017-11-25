package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.config.databaseForTable
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.models.SimpleModel_Table
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