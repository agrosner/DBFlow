package com.dbflow5.livedata

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.Observer
import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.query.select
import com.dbflow5.structure.insert
import com.nhaarman.mockitokotlin2.mock
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
    fun live_data_executes_for_a_few_model_queries() {
        val data = (select from LiveDataModel::class)
                .toLiveData { db, queriable -> queriable.queryList(db) }

        val observer = mock<Observer<MutableList<LiveDataModel>>>()
        val lifecycle = LifecycleRegistry(mock())
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        data.observeForever(observer)

        val value = data.value!!
        assert(value.isEmpty())

        database<TestDatabase>()
                .beginTransactionAsync { db ->
                    (0..2).forEach {
                        LiveDataModel(id = "$it", name = it).insert(db)
                    }
                }
                .execute()

        database<TestDatabase>().tableObserver.checkForTableUpdates()

        val value2 = data.value!!
        assert(value2.size == 3) { "expected ${value2.size} == 3" }
    }
}