package com.dbflow5.paging

import androidx.paging.PagedList
import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.config.writableTransaction
import com.dbflow5.models.SimpleModel
import com.dbflow5.query.select
import com.dbflow5.simpleModelAdapter
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Description:
 */
class QueryDataSourceTest : BaseUnitTest() {

    @Test
    fun testLoadInitialParams() = runBlockingTest {
        database<TestDatabase>().writableTransaction {
            (0 until 100).forEach {
                simpleModelAdapter.save(SimpleModel("$it"))
            }

            val factory = (select from simpleModelAdapter).toDataSourceFactory(this.db)
            val list = PagedList.Builder(
                factory.create(),
                PagedList.Config.Builder()
                    .setPageSize(3)
                    .setPrefetchDistance(6)
                    .setEnablePlaceholders(true).build()
            )
                .setFetchExecutor { it.run() } // run on main
                .setNotifyExecutor { it.run() }
                .build()

            assertEquals(100, list.size)

            list.forEachIndexed { index, simpleModel ->
                list.loadAround(index)
                assertEquals(index, simpleModel.name?.toInt())

                // assert we don't run over somehow.
                assertTrue(index < 100)
            }
        }
    }
}