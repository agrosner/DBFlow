package com.dbflow5.livedata

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.Observer
import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.beginTransactionAsync
import com.dbflow5.config.database
import com.dbflow5.config.readableTransaction
import com.dbflow5.liveDataModelAdapter
import com.dbflow5.query.select
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

/**
 * Description:
 */
class LiveDataTest : BaseUnitTest() {

    @get:Rule
    val rule: TestRule = InstantTaskExecutorRule()

    @Test
    fun live_data_executes_for_a_few_model_queries() = runBlockingTest {
        database<TestDatabase> { db ->
            val data = db.readableTransaction {
                (select from liveDataModelAdapter)
                    .toLiveData(db) { queryList(it) }
            }

            val observer = mock<Observer<List<LiveDataModel>>>()
            val lifecycle = LifecycleRegistry(mock())
            lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

            data.observeForever(observer)

            val value = data.value!!
            assert(value.isEmpty())

            db.beginTransactionAsync {
                (0..2).forEach {
                    liveDataModelAdapter.save(LiveDataModel(id = "$it", name = it))
                }
            }
                .enqueue()

            db.tableObserver.checkForTableUpdates()

            val value2 = data.value!!
            assert(value2.size == 3) { "expected ${value2.size} == 3" }
        }
    }
}