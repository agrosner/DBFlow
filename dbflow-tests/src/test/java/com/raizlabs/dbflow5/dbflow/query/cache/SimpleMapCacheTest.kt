package com.raizlabs.dbflow5.dbflow.query.cache

import com.raizlabs.dbflow5.dbflow.BaseUnitTest
import com.raizlabs.dbflow5.dbflow.models.SimpleModel
import com.raizlabs.dbflow5.query.cache.SimpleMapCache
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SimpleMapCacheTest : BaseUnitTest() {

    @Test
    fun validateCacheAddRemove() {
        val cache = SimpleMapCache<SimpleModel>(10)
        cache.addModel("1", SimpleModel("1"))

        assertEquals("1", cache["1"]!!.name)
        assertEquals(1, cache.cache.size)

        cache.removeModel("1")

        assertTrue(cache.cache.isEmpty())
    }

    @Test
    fun validateCacheClear() {
        val cache = SimpleMapCache<SimpleModel>(10)
        cache.addModel("1", SimpleModel("1"))
        cache.addModel("2", SimpleModel("2"))
        assertEquals(2, cache.cache.size)

        cache.clear()

        assertTrue(cache.cache.isEmpty())
    }
}