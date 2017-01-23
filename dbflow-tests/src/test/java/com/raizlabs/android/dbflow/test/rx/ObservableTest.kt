package com.raizlabs.android.dbflow.test.rx

import com.raizlabs.android.dbflow.rx.language.RXSQLite
import com.raizlabs.android.dbflow.test.FlowTestCase
import com.raizlabs.android.dbflow.test.structure.TestModel1
import org.junit.Test

/**
 * Description:
 */
class ObservableTest : FlowTestCase() {

    @Test
    fun testCanObserve() {

        (0..10).forEach {
            TestModel1().apply {
                name = it.toString()
            }.save()
        }

        var count = 0

        val toObservable = RXSQLite.select()
                .from(TestModel1::class.java)
                .queryStreamResults()
        toObservable.subscribe {
            count++
            assert(it != null)
        }

        assert(count == 10)
    }
}