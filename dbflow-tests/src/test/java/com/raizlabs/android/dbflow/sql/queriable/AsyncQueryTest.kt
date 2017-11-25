package com.raizlabs.android.dbflow.sql.queriable

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.config.databaseForTable
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.sql.language.CursorResult
import com.raizlabs.android.dbflow.sql.language.select
import com.raizlabs.android.dbflow.structure.save
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AsyncQueryTest : BaseUnitTest() {

    @Test
    fun testQueryResult() {
        databaseForTable<SimpleModel> {
            SimpleModel("name").save()

            var model: SimpleModel? = null
            (select from SimpleModel::class).async result { _, result ->
                model = result
            }
            assertNotNull(model)
            assertEquals("name", model?.name)
        }
    }

    @Test
    fun testQueryListResult() {
        databaseForTable<SimpleModel> {
            SimpleModel("name").save()
            SimpleModel("name2").save()

            var list = mutableListOf<SimpleModel>()
            (select from SimpleModel::class).async list { _, mutableList ->
                list = mutableList.toMutableList()
            }
            assertEquals(2, list.size)
        }
    }

    @Test
    fun testQueryListCursorResult() {
        databaseForTable<SimpleModel> {
            SimpleModel("name").save()
            SimpleModel("name2").save()

            var result: CursorResult<SimpleModel>? = null
            (select from SimpleModel::class).async cursorResult { _, cursorResult ->
                result = cursorResult
            }
            assertNotNull(result)
            assertEquals(2L, result?.count)
            result?.close()
        }
    }
}