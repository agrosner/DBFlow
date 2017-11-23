package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.assertEquals
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.models.SimpleModel_Table.name
import com.raizlabs.android.dbflow.models.TwoColumnModel_Table.id
import com.raizlabs.android.dbflow.sql.Query
import org.junit.Test

class SetTest : BaseUnitTest() {

    @Test
    fun validateSetWithConditions() {
        assertEquals("SET `name`='name'",
                Set(object : Query {
                    override val query = ""
                }, SimpleModel::class.java).conditions(name.`is`("name")))
    }

    @Test
    fun validateMultipleConditions() {
        assertEquals("SET `name`='name', `id`=0",
                Set(object : Query {
                    override val query = ""
                }, SimpleModel::class.java)
                        .conditions(name.`is`("name"), id.`is`(0)))
    }
}