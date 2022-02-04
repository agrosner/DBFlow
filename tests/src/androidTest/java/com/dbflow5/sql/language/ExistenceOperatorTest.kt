package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.query.ExistenceOperator
import com.dbflow5.query.select
import org.junit.Assert.assertEquals
import org.junit.Test

class ExistenceOperatorTest : BaseUnitTest() {


    @Test
    fun validateQuery() {
        assertEquals(
            "EXISTS (SELECT * FROM `SimpleModel` WHERE `name`='name')",
            ExistenceOperator(
                (select from database<TestDatabase>().simpleModelAdapter
                    where SimpleModel_Table.name.eq("name"))
            )
                .query.trim()
        )
    }
}