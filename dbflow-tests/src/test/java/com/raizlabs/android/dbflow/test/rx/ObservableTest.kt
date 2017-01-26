package com.raizlabs.android.dbflow.test.rx

import com.raizlabs.android.dbflow.rx.language.RXSQLite
import com.raizlabs.android.dbflow.test.FlowTestCase
import com.raizlabs.android.dbflow.test.structure.TestModel1
import com.raizlabs.android.dbflow.test.structure.TestModel1_Table
import org.junit.Assert.assertEquals
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

        RXSQLite.select()
            .from(TestModel1::class.java)
            .queryStreamResults()
            .toBlocking().subscribe {
            count++
            assert(it != null)
        }

        assertEquals(10, count)
    }

    @Test
    fun testCanObserveModelChanges() {

        var count = 0

        RXSQLite.select()
            .from(TestModel1::class.java)
            .observeOnTableChanges()
            .subscribe {
                count++
            }

        val model = TestModel1().apply { name = "1" }
        model.insert()
        model.update()
        model.delete()

        assertEquals(3, count)
    }

    @Test
    fun testCanObserveWrapperChanges() {

        var count = 0

        RXSQLite.select()
            .from(TestModel1::class.java)
            .observeOnTableChanges()
            .subscribe {
                count++
            }

        RXSQLite.insert(TestModel1::class.java)
            .columnValues(TestModel1_Table.name.eq("Test"))
            .executeInsert().toBlocking().value()

        assertEquals(3, count)
    }
}