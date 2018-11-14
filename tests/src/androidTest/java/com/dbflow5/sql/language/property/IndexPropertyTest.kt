package com.dbflow5.sql.language.property

import com.dbflow5.BaseUnitTest
import com.dbflow5.config.databaseForTable
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.query.property.IndexProperty
import org.junit.Assert.assertEquals
import org.junit.Test

class IndexPropertyTest : BaseUnitTest() {


    @Test
    fun validateIndexProperty() {
        databaseForTable<SimpleModel> { db ->
            val prop = IndexProperty("Index", true, SimpleModel::class.java,
                    SimpleModel_Table.name)
            prop.createIfNotExists(db)
            prop.drop(db)
            assertEquals("`Index`", prop.indexName)
        }
    }
}