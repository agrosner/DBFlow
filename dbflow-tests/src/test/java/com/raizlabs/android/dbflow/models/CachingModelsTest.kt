package com.raizlabs.android.dbflow.models

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.list
import com.raizlabs.android.dbflow.kotlinextensions.result
import com.raizlabs.android.dbflow.kotlinextensions.save
import com.raizlabs.android.dbflow.kotlinextensions.select
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Description: Tests to ensure caching works as expected.
 */
class CachingModelsTest : BaseUnitTest() {

    @Test
    fun testSimpleCache() {
        val list = arrayListOf<SimpleCacheObject>()
        (0..9).forEach {
            val simpleCacheObject = SimpleCacheObject("$it")
            simpleCacheObject.save()
            list += simpleCacheObject
        }

        val loadedList = (select from SimpleCacheObject::class).list

        loadedList.forEachIndexed { index, simpleCacheObject ->
            assertEquals(list[index], simpleCacheObject)
        }
    }

    @Test
    fun testComplexObject() {
        val path = Path("1", "Path")
        path.save()

        val coordinate = Coordinate(40.5, 84.0, path)
        coordinate.save()

        val oldPath = coordinate.path

        val loadedCoordinate = (select from Coordinate::class).result!!
        assertEquals(coordinate, loadedCoordinate)

        // we want to ensure relationships reloaded.
        assertNotEquals(oldPath, loadedCoordinate.path)
    }
}
