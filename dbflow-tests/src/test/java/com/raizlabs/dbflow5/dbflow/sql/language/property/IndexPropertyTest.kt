package com.raizlabs.dbflow5.dbflow.sql.language.property

import com.raizlabs.dbflow5.dbflow.BaseUnitTest
import com.raizlabs.dbflow5.config.databaseForTable
import com.raizlabs.dbflow5.dbflow.models.SimpleModel
import com.raizlabs.dbflow5.dbflow.models.SimpleModel_Table
import com.raizlabs.dbflow5.query.property.IndexProperty
import org.junit.Assert.assertEquals
import org.junit.Test

class IndexPropertyTest : BaseUnitTest() {


    @Test
    fun validateIndexProperty() {
        databaseForTable<SimpleModel> {
            val prop = IndexProperty("Index", true, SimpleModel::class.java,
                    SimpleModel_Table.name)
            prop.createIfNotExists(this)
            prop.drop(this)
            assertEquals("`Index`", prop.indexName)
        }
    }
}