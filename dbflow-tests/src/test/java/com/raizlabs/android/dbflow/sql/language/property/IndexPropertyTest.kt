package com.raizlabs.android.dbflow.sql.language.property

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.config.writableDatabaseForTable
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.models.SimpleModel_Table
import org.junit.Assert.assertEquals
import org.junit.Test

class IndexPropertyTest : BaseUnitTest() {


    @Test
    fun validateIndexProperty() = writableDatabaseForTable<SimpleModel> {

        val prop = IndexProperty("Index", true, SimpleModel::class.java,
                SimpleModel_Table.name)
        prop.createIfNotExists(this)
        prop.drop(this)
        assertEquals("`Index`", prop.indexName)
    }
}