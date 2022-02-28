package com.dbflow5.sql.language.property

import com.dbflow5.TestDatabase_Database
import com.dbflow5.dropIndex
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.query.operations.indexProperty
import com.dbflow5.test.DatabaseTestRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class IndexPropertyTest {

    @get:Rule
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