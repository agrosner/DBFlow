package com.dbflow5.models

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
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
    fun testSimpleCache() {
        database<TestDatabase> { db ->
            val list = arrayListOf<SimpleCacheObject>()
            (0..9).forEach {
                val simpleCacheObject = SimpleCacheObject("$it")
                simpleCacheObject.save(db)
                list += simpleCacheObject
            }

            val loadedList = (select from SimpleCacheObject::class).queryList(db)

            loadedList.forEachIndexed { index, simpleCacheObject ->
                assertEquals(list[index], simpleCacheObject)
            }
        }
    }

    @Test
    fun testComplexObject() {
        database<TestDatabase> { db ->
            val path = Path("1", "Path")
            path.save(db)

            val coordinate = Coordinate(40.5, 84.0, path)
            coordinate.save(db)

            val oldPath = coordinate.path

            val loadedCoordinate = (select from Coordinate::class).querySingle(db)!!
            assertEquals(coordinate, loadedCoordinate)

            // we want to ensure relationships reloaded.
            assertNotEquals(oldPath, loadedCoordinate.path)
        }
    }
}
