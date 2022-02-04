package com.dbflow5.models

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.config.readableTransaction
import com.dbflow5.config.writableTransaction
import com.dbflow5.fts4ModelAdapter
import com.dbflow5.fts4VirtualModel2Adapter
import com.dbflow5.query.insert
import com.dbflow5.query.offsets
import com.dbflow5.query.property.docId
import com.dbflow5.query.property.tableName
import com.dbflow5.query.select
import com.dbflow5.query.snippet
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

            val rows = (insert(
                fts4ModelAdapter,
                docId,
                Fts4VirtualModel2_Table.name
            ) select (select(Fts4Model_Table.id, Fts4Model_Table.name) from fts4ModelAdapter))
                .executeInsert()
            assertTrue(rows > 0)
        }
    }

    @Test
    fun match_query() = runBlockingTest {
        validate_fts4_created()
        database<TestDatabase>().readableTransaction {
            (select from fts4VirtualModel2Adapter where (tableName<Fts4VirtualModel2>() match "FTSBABY"))
                .requireSingle()
        }
    }

    @Test
    fun offsets_query() = runBlockingTest {
        validate_fts4_created()
        database<TestDatabase>().readableTransaction {
            val value = (select(offsets<Fts4VirtualModel2>()) from fts4ModelAdapter
                where (tableName<Fts4VirtualModel2>() match "FTSBaby"))
                .stringValue()
            assertNotNull(value)
            assertEquals(value, "0 0 0 7")
        }
    }

    @Test
    fun snippet_query() = runBlockingTest {
        database<TestDatabase>().writableTransaction {
            val model = Fts4Model(
                name = "During 30 Nov-1 Dec, 2-3oC drops. Cool in the upper portion, minimum temperature 14-16oC \n" +
                    "  and cool elsewhere, minimum temperature 17-20oC. Cold to very cold on mountaintops, \n" +
                    "  minimum temperature 6-12oC. Northeasterly winds 15-30 km/hr. After that, temperature \n" +
                    "  increases. Northeasterly winds 15-30 km/hr. "
            )
            fts4ModelAdapter.save(model)
            val rows = (insert(
                fts4ModelAdapter,
                docId,
                Fts4VirtualModel2_Table.name
            ) select (select(Fts4Model_Table.id, Fts4Model_Table.name) from fts4ModelAdapter))
                .executeInsert()
            assert(rows > 0)
            val value = (select(
                snippet<Fts4VirtualModel2>(
                    start = "[",
                    end = "]",
                    ellipses = "...",
                )
            ) from fts4VirtualModel2Adapter
                where (tableName<Fts4VirtualModel2>() match "\"min* tem*\""))
                .stringValue()
            assertNotNull(value)
            assertEquals(
                value, "...the upper portion, [minimum] [temperature] 14-16oC \n" +
                    "  and cool elsewhere, [minimum] [temperature] 17-20oC. Cold..."
            )
        }
    }
}
