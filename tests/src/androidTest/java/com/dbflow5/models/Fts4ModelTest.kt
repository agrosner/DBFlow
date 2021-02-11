package com.dbflow5.models

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.query.insert
import com.dbflow5.query.offsets
import com.dbflow5.query.property.docId
import com.dbflow5.query.property.tableName
import com.dbflow5.query.select
import com.dbflow5.query.snippet
import com.dbflow5.structure.save
import org.junit.Test

/**
 * Description:
 */
class Fts4ModelTest : BaseUnitTest() {

    @Test
    fun validate_fts4_created() {
        database<TestDatabase> { db ->
            val model = Fts4Model(name = "FTSBABY")
            model.save(db)

            val rows = (insert<Fts4VirtualModel2>(
                docId,
                Fts4VirtualModel2_Table.name) select (select(Fts4Model_Table.id, Fts4Model_Table.name) from Fts4Model::class))
                .executeInsert(db)
            assert(rows > 0)


        }
    }

    @Test
    fun match_query() {
        validate_fts4_created()
        database<TestDatabase> { db ->
            val model = (select from Fts4VirtualModel2::class where (tableName<Fts4VirtualModel2>() match "FTSBABY"))
                .querySingle(db)
            assert(model != null)
        }
    }

    @Test
    fun offsets_query() {
        validate_fts4_created()
        database<TestDatabase> { db ->
            val value = (select(offsets<Fts4VirtualModel2>()) from Fts4VirtualModel2::class
                where (tableName<Fts4VirtualModel2>() match "FTSBaby"))
                .stringValue(db)
            assert(value != null)
            assert(value == "0 0 0 7")
        }
    }

    @Test
    fun snippet_query() {
        database<TestDatabase> { db ->
            val model = Fts4Model(name = "During 30 Nov-1 Dec, 2-3oC drops. Cool in the upper portion, minimum temperature 14-16oC \n" +
                "  and cool elsewhere, minimum temperature 17-20oC. Cold to very cold on mountaintops, \n" +
                "  minimum temperature 6-12oC. Northeasterly winds 15-30 km/hr. After that, temperature \n" +
                "  increases. Northeasterly winds 15-30 km/hr. ")
            model.save(db)
            val rows = (insert<Fts4VirtualModel2>(
                docId,
                Fts4VirtualModel2_Table.name) select (select(Fts4Model_Table.id, Fts4Model_Table.name) from Fts4Model::class))
                .executeInsert(db)
            assert(rows > 0)
            val value = (select(snippet<Fts4VirtualModel2>(
                start = "[",
                end = "]",
                ellipses = "...",
            )) from Fts4VirtualModel2::class
                where (tableName<Fts4VirtualModel2>() match "\"min* tem*\""))
                .stringValue(db)
            assert(value != null)
            assert(value == "...the upper portion, [minimum] [temperature] 14-16oC \n" +
                "  and cool elsewhere, [minimum] [temperature] 17-20oC. Cold...")
        }
    }
}