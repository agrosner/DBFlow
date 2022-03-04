package com.dbflow5.paging

import androidx.paging.PagedList
import com.dbflow5.TestDatabase_Database
import com.dbflow5.models.SimpleModel
import com.dbflow5.query.select
import com.dbflow5.simpleModelAdapter
import com.dbflow5.test.DatabaseTestRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Description:
 */
class QueryDataSourceTest {

    
    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun testLoadInitialParams() = dbRule.runTest {
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