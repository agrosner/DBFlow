package com.dbflow5.test.sql.language.property

import com.dbflow5.test.TestDatabase_Database
import com.dbflow5.dropIndex
import com.dbflow5.test.SimpleModel_Table
import com.dbflow5.query.operations.indexProperty
import com.dbflow5.test.DatabaseTestRule
import kotlin.test.Test
import kotlin.test.assertEquals

class IndexPropertyTest {

    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun validateIndexProperty() = dbRule.runTest {
        val prop = indexProperty(
            "Index", true,
            SimpleModel_Table.name
        )

        prop.index.execute(db)
        dropIndex(db, prop.name)
        assertEquals("`Index`", prop.name)
    }
}