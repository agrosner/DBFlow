package com.raizlabs.android.dbflow.rx.language

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.config.writableDatabaseForTable
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.sql.language.select
import com.raizlabs.android.dbflow.structure.save
import org.junit.Assert.assertEquals
import org.junit.Test

class CursorResultSubscriberTest : BaseUnitTest() {


    @Test
    fun testCanQueryStreamResults() = writableDatabaseForTable<SimpleModel> {
        (0..9).forEach { SimpleModel("$it").save() }

        var count = 0
        (select from SimpleModel::class).rx()
                .queryStreamResults()
                .toBlocking()
                .subscribe {
                    count++
                    assert(it != null)
                }

        assertEquals(10, count)
    }

}