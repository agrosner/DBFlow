package com.raizlabs.dbflow5.paging

import android.arch.paging.PagedList
import com.raizlabs.dbflow5.BaseUnitTest
import com.raizlabs.dbflow5.TestDatabase
import com.raizlabs.dbflow5.config.database
import com.raizlabs.dbflow5.models.SimpleModel
import com.raizlabs.dbflow5.query.select
import com.raizlabs.dbflow5.structure.save
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Description:
 */
class ModelQueriableDataSourceTest : BaseUnitTest() {

    @Test
    fun testLoadInitialParams() {
        database<TestDatabase> {
            (0 until 100).forEach { SimpleModel("$it").save(this) }


            val factory = (select from SimpleModel::class).toDataSourceFactory(this)
            val list = PagedList.Builder(factory.create(),
                PagedList.Config.Builder()
                    .setPageSize(5)
                    .setPrefetchDistance(10)
                    .setEnablePlaceholders(true).build())
                .setBackgroundThreadExecutor { it.run() } // run on main
                .setMainThreadExecutor { it.run() }
                .build()

            assertEquals(100, list.size)

            list.forEachIndexed { index, simpleModel ->
                list.loadAround(index)
                assertEquals(index, simpleModel.name?.toInt())
            }
        }
    }
}