package com.raizlabs.android.dbflow.rx.language

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.sql.language.from
import com.raizlabs.android.dbflow.kotlinextensions.save
import com.raizlabs.android.dbflow.sql.language.select
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.rx.kotlinextensions.rx
import org.junit.Assert.assertEquals
import org.junit.Test

class CursorResultSubscriberTest : BaseUnitTest() {


    @Test
    fun testCanQueryStreamResults() {
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