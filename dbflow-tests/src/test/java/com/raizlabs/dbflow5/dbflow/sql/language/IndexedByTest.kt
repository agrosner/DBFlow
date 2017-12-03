package com.raizlabs.dbflow5.dbflow.sql.language

import com.raizlabs.dbflow5.dbflow.BaseUnitTest
import com.raizlabs.dbflow5.config.databaseForTable
import com.raizlabs.dbflow5.dbflow.models.SimpleModel
import com.raizlabs.dbflow5.dbflow.models.SimpleModel_Table
import com.raizlabs.dbflow5.query.property.IndexProperty
import org.junit.Assert.assertEquals
import org.junit.Test

class IndexedByTest : BaseUnitTest() {

    @Test
    fun validateQuery() {
        databaseForTable<SimpleModel> {
            val indexed = (select from SimpleModel::class)
                    .indexedBy(IndexProperty("Index", false, SimpleModel::class.java, SimpleModel_Table.name))
            assertEquals("SELECT * FROM `SimpleModel` INDEXED BY `Index`", indexed.query.trim())
        }
    }
}