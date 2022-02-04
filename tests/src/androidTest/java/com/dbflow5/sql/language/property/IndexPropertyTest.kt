package com.dbflow5.sql.language.property

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.query.property.IndexProperty
import org.junit.Assert.assertEquals
import org.junit.Test

class IndexPropertyTest : BaseUnitTest() {


    @Test
    fun validateIndexProperty() {
        database<TestDatabase> { db ->
            val prop = IndexProperty(
                "Index", true, db.simpleModelAdapter,
                SimpleModel_Table.name
            )
            prop.createIfNotExists(db)
            prop.drop(db)
            assertEquals("`Index`", prop.indexName)
        }
    }
}