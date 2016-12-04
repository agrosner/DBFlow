package com.raizlabs.android.dbflow.test.structure.caching

import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.sql.language.Select
import com.raizlabs.android.dbflow.structure.ModelAdapter
import com.raizlabs.android.dbflow.structure.cache.ModelCache
import com.raizlabs.android.dbflow.test.FlowTestCase

import org.junit.Test

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue

class CacheableModelTest : FlowTestCase() {

    @Test
    fun testCacheableModel() {

        Delete.table(CacheableModel::class.java)

        val model = CacheableModel()

        val modelCache = FlowManager.getModelAdapter(CacheableModel::class.java).modelCache
        for (i in 0..99) {
            model.name = "Test"
            model.save()
            assertTrue(model.exists())

            val id = model.id
            val cacheableModel = modelCache.get(id)
            assertNotNull(cacheableModel)

            assertEquals(SQLite.select().from(CacheableModel::class.java).where(CacheableModel_Table.id.`is`(id))
                    .querySingle(), cacheableModel)

            model.delete()
            assertNull(modelCache.get(id))
        }

        Delete.table(CacheableModel::class.java)
    }

    @Test
    fun testCacheableModel2() {
        Delete.table(CacheableModel2::class.java)

        val model = CacheableModel2()

        val modelCache = FlowManager.getModelAdapter(CacheableModel2::class.java).modelCache
        for (i in 0..99) {
            model.id = i
            model.save()

            val id = model.id
            val cacheableModel = modelCache.get(id)
            assertNotNull(cacheableModel)

            assertEquals(Select().from(CacheableModel2::class.java)
                    .where(CacheableModel2_Table.id.`is`(id))
                    .querySingle(), cacheableModel)

            model.delete()
            assertNull(modelCache.get(id))
        }

        Delete.table(CacheableModel2::class.java)
    }

    @Test
    fun testCacheableModel3() {
        Delete.table(CacheableModel3::class.java)

        val cacheableModel3 = CacheableModel3()

        val modelCache = FlowManager.getModelAdapter(CacheableModel3::class.java).modelCache
        for (i in 0..19) {
            cacheableModel3.number = i
            cacheableModel3.cache_id = "model" + i
            cacheableModel3.save()

            val id = cacheableModel3.cache_id
            val cacheableModel = modelCache.get(id)
            assertNotNull(cacheableModel)

            assertEquals(Select().from(CacheableModel3::class.java)
                    .where(CacheableModel3_Table.cache_id.`is`(id))
                    .querySingle(), cacheableModel)

            cacheableModel3.delete()
            assertNull(modelCache.get(id))
        }

        Delete.table(CacheableModel3::class.java)

    }

    @Test
    fun testCacheableModel4() {
        val model4s = SQLite.select()
                .from(CacheableModel4::class.java)
                .where(CacheableModel4_Table.id.eq(4))
                .queryList()
    }

    @Test
    fun testMultiplePrimaryKey() {
        Delete.table(MultipleCacheableModel::class.java)

        val cacheableModel = MultipleCacheableModel()
        val modelAdapter = FlowManager.getModelAdapter(MultipleCacheableModel::class.java)
        val modelCache = modelAdapter.modelCache
        val values = arrayOfNulls<Any>(modelAdapter.cachingColumns.size)
        for (i in 0..24) {
            cacheableModel.latitude = i.toDouble()
            cacheableModel.longitude = 25.0
            cacheableModel.save()

            val model = modelCache.get(MultipleCacheableModel.multiKeyCacheModel
                    .getCachingKey(modelAdapter.getCachingColumnValuesFromModel(values, cacheableModel)))
            assertNotNull(model)
            assertEquals(cacheableModel, model)
            assertEquals(SQLite.select().from(MultipleCacheableModel::class.java)
                    .where(MultipleCacheableModel_Table.latitude.eq(cacheableModel.latitude))
                    .and(MultipleCacheableModel_Table.longitude.eq(cacheableModel.longitude)).querySingle(), model)

            model.delete()
            assertNull(modelCache.get(MultipleCacheableModel.multiKeyCacheModel
                    .getCachingKey(modelAdapter.getCachingColumnValuesFromModel(values, model))))
        }

        Delete.table(MultipleCacheableModel::class.java)
    }
}
