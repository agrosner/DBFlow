package com.dbflow5.query.cache

import com.dbflow5.BaseUnitTest
import com.dbflow5.models.NumberModel
import org.junit.Assert
import org.junit.Test

class ModelLruCacheTest : BaseUnitTest() {


    @Test
    fun validateCacheAddRemove() {
        val cache = SimpleMapCache<NumberModel>(10)
        cache.addModel(1, NumberModel(1))

        Assert.assertEquals(1, cache[1]!!.id)
        Assert.assertEquals(1, cache.cache.size)

        cache.removeModel(1)

        Assert.assertTrue(cache.cache.isEmpty())
    }

    @Test
    fun validateCacheClear() {
        val cache = SimpleMapCache<NumberModel>(10)
        cache.addModel(1, NumberModel(1))
        cache.addModel(2, NumberModel(2))
        Assert.assertEquals(2, cache.cache.size)

        cache.clear()

        Assert.assertTrue(cache.cache.isEmpty())
    }
}