package com.raizlabs.android.dbflow.test.rx

import com.raizlabs.android.dbflow.kotlinextensions.select
import com.raizlabs.android.dbflow.rx.kotlinextensions.rx
import com.raizlabs.android.dbflow.test.FlowTestCase
import com.raizlabs.android.dbflow.test.structure.TestModel1
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test

/**
 * Description:
 */
class ObservableTest : FlowTestCase() {

    @Test
    fun testCanObserve() {

        (0..9).forEach {
            TestModel1().apply {
                name = it.toString()
            }.save()
        }

        var count = 0

        select.from(TestModel1::class.java)
            .rx()
            .queryStreamResults()
            .toBlocking().subscribe {
            count++
            assert(it != null)
        }

        assertEquals(10, count)
    }

    @Ignore
    @Test
    fun testCanObserveModelChanges() {

        var count = 0

        select.from(TestModel1::class.java)
            .rx()
            .observeOnTableChanges()
            .toBlocking()
            .subscribe {
                count++
            }

        val model = TestModel1().apply { name = "1" }
        model.insert()
        model.update()
        model.delete()

        assertEquals(3, count)

    }

    @Ignore
    @Test
    fun testCanObserveWrapperChanges() {

        var count = 0

        select.from(TestModel1::class.java)
            .rx()
            .observeOnTableChanges()
            .toBlocking()
            .subscribe {
                count++
            }

        assertEquals(3, count)
    }
}