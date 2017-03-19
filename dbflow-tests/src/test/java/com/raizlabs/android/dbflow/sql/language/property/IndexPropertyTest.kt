package com.raizlabs.android.dbflow.sql.language.property

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.models.SimpleModel_Table
import org.junit.Assert.assertEquals
import org.junit.Test

class IndexPropertyTest : BaseUnitTest() {


    @Test
    fun validateIndexProperty() {

        val prop = IndexProperty<SimpleModel>("Index", true, SimpleModel::class.java,
            SimpleModel_Table.name)
        prop.createIfNotExists()
        prop.drop()
        assertEquals("`Index`", prop.indexName)
    }
}