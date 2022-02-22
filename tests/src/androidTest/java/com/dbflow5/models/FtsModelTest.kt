package com.dbflow5.models

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.config.readableTransaction
import com.dbflow5.config.writableTransaction
import com.dbflow5.fts4ModelAdapter
import com.dbflow5.fts4VirtualModel2Adapter
import com.dbflow5.query.StringResultFactory
import com.dbflow5.query.insert
import com.dbflow5.query.operations.docId
import com.dbflow5.query.operations.match
import com.dbflow5.query.methods.offsets
import com.dbflow5.query.methods.snippet
import com.dbflow5.query.operations.tableNameLiteral
import com.dbflow5.query.select
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
            val updated = fts4ModelAdapter.save(model)
            assertTrue(updated.id > 0)
        }

        database<TestDatabase>().readableTransaction {
            val rowID = (fts4VirtualModel2Adapter.insert(
                fts4VirtualModel2Adapter.docId(),
                Fts4VirtualModel2_Table.name
            ) select fts4ModelAdapter.select(Fts4Model_Table.id, Fts4Model_Table.name)).execute()
            assertTrue(rowID > 0)
        }
    }

    @Test
    fun match_query() = runBlockingTest {
        validate_fts4_created()
        database<TestDatabase>().readableTransaction {
            (fts4VirtualModel2Adapter.select() where (
                fts4VirtualModel2Adapter.tableNameLiteral() match "FTSBABY"))
                .single()
        }
    }

    @Test
    fun offsets_query() = runBlockingTest {
        validate_fts4_created()
        val value = database<TestDatabase>().readableTransaction {
            (fts4VirtualModel2Adapter.select(
                StringResultFactory,
                offsets<Fts4VirtualModel2>()
            )
                where (fts4VirtualModel2Adapter.tableNameLiteral() match "FTSBaby"))
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
            (fts4VirtualModel2Adapter.insert(
                fts4VirtualModel2Adapter.docId(),
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
                ) where (fts4VirtualModel2Adapter.tableNameLiteral() match "\"min* tem*\""))
                    .execute()
            }
        assertNotNull(value)
        assertEquals(
            value.value, "...the upper portion, [minimum] [temperature] 14-16oC \n" +
                "  and cool elsewhere, [minimum] [temperature] 17-20oC. Cold..."
        )
    }
}
