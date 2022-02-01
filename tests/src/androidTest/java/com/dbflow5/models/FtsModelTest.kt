package com.dbflow5.models

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.config.readableTransaction
import com.dbflow5.config.writableTransaction
import com.dbflow5.fts4Model
import com.dbflow5.query.insert
import com.dbflow5.query.offsets
import com.dbflow5.query.property.docId
import com.dbflow5.query.property.tableName
import com.dbflow5.query.select
import com.dbflow5.query.snippet
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

/**
 * Description:
 */
class FtsModelTest : BaseUnitTest() {

    @Test
    fun validate_fts4_created() = runBlockingTest {
        database<TestDatabase>().writableTransaction {
            val model = Fts4Model(name = "FTSBABY")
            fts4Model.save(model)

            val rows = (insert<Fts4VirtualModel2>(
                docId,
                Fts4VirtualModel2_Table.name
            ) select (select(Fts4Model_Table.id, Fts4Model_Table.name) from Fts4Model::class))
                .executeInsert()
            assert(rows > 0)
        }
    }

    @Test
    fun match_query() = runBlockingTest {
        validate_fts4_created()
        database<TestDatabase>().readableTransaction {
            val model =
                (select from Fts4VirtualModel2::class where (tableName<Fts4VirtualModel2>() match "FTSBABY"))
                    .querySingle()
            assert(model != null)
        }
    }

    @Test
    fun offsets_query() = runBlockingTest {
        validate_fts4_created()
        database<TestDatabase>().readableTransaction {
            val value = (select(offsets<Fts4VirtualModel2>()) from Fts4VirtualModel2::class
                where (tableName<Fts4VirtualModel2>() match "FTSBaby"))
                .stringValue()
            assert(value != null)
            assert(value == "0 0 0 7")
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
            fts4Model.save(model)
            val rows = (insert<Fts4VirtualModel2>(
                docId,
                Fts4VirtualModel2_Table.name
            ) select (select(Fts4Model_Table.id, Fts4Model_Table.name) from Fts4Model::class))
                .executeInsert()
            assert(rows > 0)
            val value = (select(
                snippet<Fts4VirtualModel2>(
                    start = "[",
                    end = "]",
                    ellipses = "...",
                )
            ) from Fts4VirtualModel2::class
                where (tableName<Fts4VirtualModel2>() match "\"min* tem*\""))
                .stringValue()
            assert(value != null)
            assert(
                value == "...the upper portion, [minimum] [temperature] 14-16oC \n" +
                    "  and cool elsewhere, [minimum] [temperature] 17-20oC. Cold..."
            )
        }
    }
}
