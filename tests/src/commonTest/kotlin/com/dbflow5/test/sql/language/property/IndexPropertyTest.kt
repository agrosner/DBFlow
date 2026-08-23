package com.dbflow5.test.sql.language.property

import com.dbflow5.dropIndex
import com.dbflow5.query.operations.indexProperty
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.test.SimpleModel
import com.dbflow5.test.TestDatabase_Database
import com.dbflow5.test.TestRule
import kotlin.test.Test
import kotlin.test.assertEquals

class IndexPropertyTest : TestRule() {

    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun validateIndexProperty() = dbRule.runTest {
        val prop = indexProperty(
            "Index", true,
            SimpleModel.name
        )

        prop.index.execute(db)
        dropIndex(db, prop.name)
        assertEquals("`Index`", prop.name)
    }
}