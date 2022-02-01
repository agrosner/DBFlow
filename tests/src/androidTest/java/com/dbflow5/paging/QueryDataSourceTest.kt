package com.dbflow5.paging

import androidx.paging.PagedList
import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.assertThrowsException
import com.dbflow5.config.database
import com.dbflow5.config.writableTransaction
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.query.select
import com.dbflow5.query.set
import com.dbflow5.query.update
import com.dbflow5.simpleModel
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
                simpleModel.save(SimpleModel("$it"))
            }

            val factory = (select from SimpleModel::class).toDataSourceFactory(this.db)
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

    @Test
    fun testThrowsErrorOnInvalidType() {
        database<TestDatabase> { db ->
            assertThrowsException(IllegalArgumentException::class) {
                (update<SimpleModel>() set (SimpleModel_Table.name.eq("name")))
                    .toDataSourceFactory(db)
                    .create()
            }
        }
    }
}