package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.query.property.IndexProperty
import com.dbflow5.query.select
import org.junit.Assert.assertEquals
import org.junit.Test

class IndexedByTest : BaseUnitTest() {

    @Test
    fun validateQuery() {
        val indexed = (select from SimpleModel::class)
            .indexedBy(
                IndexProperty(
                    "Index",
                    false,
                    SimpleModel::class,
                    SimpleModel_Table.name
                )
            )
        assertEquals("SELECT * FROM `SimpleModel` INDEXED BY `Index`", indexed.query.trim())
    }
}