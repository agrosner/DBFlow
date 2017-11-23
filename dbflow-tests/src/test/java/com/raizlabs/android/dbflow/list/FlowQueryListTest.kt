package com.raizlabs.android.dbflow.list

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.sql.language.from
import com.raizlabs.android.dbflow.sql.language.select
import org.junit.Assert.assertTrue
import org.junit.Test

class FlowQueryListTest : BaseUnitTest() {

    @Test
    fun validateBuilder() {

        val list = FlowQueryList.Builder(select from SimpleModel::class)
                .transact(true)
                .changeInTransaction(true)
                .build()

        assertTrue(list.transact)
        assertTrue(list.changeInTransaction())
    }
}