package com.raizlabs.android.dbflow.test.rx

import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.select
import com.raizlabs.android.dbflow.rx.RXExtension
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

        val toObservable = RXExtension.toObservable(select from TestModel1::class)
        toObservable.subscribe { count++ }

        assert(count == 10)

    }
}