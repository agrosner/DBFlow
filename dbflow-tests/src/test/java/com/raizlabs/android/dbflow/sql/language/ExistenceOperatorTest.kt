package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.SimpleModel
import com.raizlabs.android.dbflow.SimpleModel_Table
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.select
import com.raizlabs.android.dbflow.kotlinextensions.where
import org.junit.Assert.assertEquals
import org.junit.Test

class ExistenceOperatorTest : BaseUnitTest() {


    @Test
    fun validateQuery() {
        assertEquals("EXISTS (SELECT * FROM `SimpleModel` WHERE `name`='name')", ExistenceOperator()
                .where(select from SimpleModel::class where SimpleModel_Table.name.eq("name")).query.trim())
    }
}