package com.dbflow5.models

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.query.list
import com.dbflow5.query.result
import com.dbflow5.query.select
import com.dbflow5.structure.save
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Description: Tests to ensure caching works as expected.
 */
class CachingModelsTest : BaseUnitTest() {

    @Test
    fun testSimpleCache() = database(TestDatabase::class) {
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
    fun testComplexObject() = database(TestDatabase::class) {
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
