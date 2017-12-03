package com.raizlabs.dbflow5.rx.query

import com.raizlabs.dbflow5.BaseUnitTest
import com.raizlabs.dbflow5.config.databaseForTable
import com.raizlabs.dbflow5.models.SimpleModel
import com.raizlabs.dbflow5.query.select
import com.raizlabs.dbflow5.structure.save
import org.junit.Assert.assertEquals
import org.junit.Test

class CursorResultSubscriberTest : BaseUnitTest() {

    @Test
    fun testCanQueryStreamResults() {
        databaseForTable<SimpleModel> {
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

}