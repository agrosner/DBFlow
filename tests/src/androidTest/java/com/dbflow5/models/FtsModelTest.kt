package com.dbflow5.models

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.config.readableTransaction
import com.dbflow5.config.writableTransaction
import com.dbflow5.fts4ModelAdapter
import com.dbflow5.fts4VirtualModel2Adapter
import com.dbflow5.query.offsets
import com.dbflow5.query.property.docId
import com.dbflow5.query.property.tableName
import com.dbflow5.query.snippet
import com.dbflow5.query2.StringResultFactory
import com.dbflow5.query2.insert
import com.dbflow5.query2.select
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Description:
 */
class FtsModelTest : BaseUnitTest() {

    @Test
    fun validate_fts4_created() = runBlockingTest {
        database<TestDatabase>().writableTransaction {
            val model = Fts4Model(name = "FTSBABY")
            fts4ModelAdapter.save(model)

            val rowID = (fts4ModelAdapter.insert(
                docId,
                Fts4VirtualModel2_Table.name
            ) select fts4ModelAdapter.select(Fts4Model_Table.id, Fts4Model_Table.name))
                .execute()
            assertTrue(rowID > 0)
        }
    }

    @Test
    fun match_query() = runBlockingTest {
        validate_fts4_created()
        database<TestDatabase>().readableTransaction {
            (fts4VirtualModel2Adapter.select() where (tableName<Fts4VirtualModel2>() match "FTSBABY"))
                .single()
        }
    }

    @Test
    fun offsets_query() = runBlockingTest {
        validate_fts4_created()
        val value = database<TestDatabase>().readableTransaction {
            (fts4ModelAdapter.select(
                resultFactory = StringResultFactory,
                offsets<Fts4VirtualModel2>()
            )
                where (tableName<Fts4VirtualModel2>() match "FTSBaby"))
                .execute()
        }
        assertNotNull(value)
        assertEquals(value, StringResultFactory.StringResult("0 0 0 7"))
    }

    @Test
    fun snippet_query() = runBlockingTest {
        val database = database<TestDatabase>()
        database.writableTransaction {
            val model = Fts4Model(
                name = "During 30 Nov-1 Dec, 2-3oC drops. Cool in the upper portion, minimum temperature 14-16oC \n" +
                    "  and cool elsewhere, minimum temperature 17-20oC. Cold to very cold on mountaintops, \n" +
                    "  minimum temperature 6-12oC. Northeasterly winds 15-30 km/hr. After that, temperature \n" +
                    "  increases. Northeasterly winds 15-30 km/hr. "
            )
            fts4ModelAdapter.save(model)
        }
        val rows = database.writableTransaction {
            (fts4ModelAdapter.insert(
                docId,
                Fts4VirtualModel2_Table.name
            ) select fts4ModelAdapter.select(Fts4Model_Table.id, Fts4Model_Table.name))
                .execute()
        }
        assert(rows > 0)
        val value =
            database.readableTransaction {
                (fts4VirtualModel2Adapter.select(
                    resultFactory = StringResultFactory,
                    snippet<Fts4VirtualModel2>(
                        start = "[",
                        end = "]",
                        ellipses = "...",
                    )
                ) where (tableName<Fts4VirtualModel2>() match "\"min* tem*\""))
                    .execute()
            }
        assertNotNull(value)
        assertEquals(
            value.value, "...the upper portion, [minimum] [temperature] 14-16oC \n" +
                "  and cool elsewhere, [minimum] [temperature] 17-20oC. Cold..."
        )
    }
}
