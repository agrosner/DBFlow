package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.config.databaseForTable
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.models.SimpleModel_Table
import com.raizlabs.android.dbflow.query.property.IndexProperty
import com.raizlabs.android.dbflow.query.select
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