package com.raizlabs.android.dbflow.rx2

import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.select
import com.raizlabs.android.dbflow.rx2.language.RXSQLite
import com.raizlabs.android.dbflow.FlowTestCase
import com.raizlabs.android.dbflow.structure.TestModel1
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Description:
 */
class RXModelQueriableTest : FlowTestCase() {


    @Test
    fun testCursorResult() {
        val rxQueriable = RXSQLite.rx(select from TestModel1::class)

        TestModel1().apply { name = "Test" }.save()
        TestModel1().apply { name = "Test2" }.save()
        TestModel1().apply { name = "Test3" }.save()

        var count = 0

        rxQueriable.queryStreamResults()
            .blockingForEach {
                count++
            }

        assertEquals(3, count)
    }

}

