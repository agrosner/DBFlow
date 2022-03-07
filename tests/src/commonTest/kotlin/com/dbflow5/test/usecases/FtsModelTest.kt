package com.dbflow5.test.usecases

import com.dbflow5.query.StringResultFactory
import com.dbflow5.query.insert
import com.dbflow5.query.methods.offsets
import com.dbflow5.query.methods.snippet
import com.dbflow5.query.operations.docId
import com.dbflow5.query.operations.match
import com.dbflow5.query.operations.tableNameLiteral
import com.dbflow5.query.select
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.test.Fts4ContentModel
import com.dbflow5.test.Fts4ContentModel_Table
import com.dbflow5.test.Fts4VirtualModel
import com.dbflow5.test.Fts4VirtualModel_Table
import com.dbflow5.test.TestDatabase_Database
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Description:
 */
class FtsModelTest {

    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun validate_fts4_created() = dbRule.runTest {
        val model = Fts4ContentModel(id = 0, name = "FTSBABY")
        val updated = fts4ContentModelAdapter.save(model)
        assertTrue(updated.id > 0)

        val rowID = (fts4VirtualModelAdapter.insert(
            fts4VirtualModelAdapter.docId(),
            Fts4VirtualModel_Table.name
        ) select fts4ContentModelAdapter.select(Fts4ContentModel_Table.id, Fts4ContentModel_Table.name)).execute()
        assertTrue(rowID > 0)
    }

    @Test
    fun match_query() = dbRule.runTest {
        validate_fts4_created()
        (fts4VirtualModelAdapter.select() where (
            fts4VirtualModelAdapter.tableNameLiteral() match "FTSBABY"))
            .single()
    }

    @Test
    fun offsets_query() = dbRule.runTest {
        validate_fts4_created()
        val value = (fts4VirtualModelAdapter.select(
            StringResultFactory,
            offsets<Fts4VirtualModel>()
        )
            where (fts4VirtualModelAdapter.tableNameLiteral() match "FTSBaby"))
            .execute()
        assertNotNull(value)
        assertEquals(value, StringResultFactory.StringResult("0 0 0 7"))
    }

    @Test
    fun snippet_query() = dbRule.runTest {
        fts4ContentModelAdapter.save(Fts4ContentModel(
            id = 0,
            name = "During 30 Nov-1 Dec, 2-3oC drops. Cool in the upper portion, minimum temperature 14-16oC \n" +
                "  and cool elsewhere, minimum temperature 17-20oC. Cold to very cold on mountaintops, \n" +
                "  minimum temperature 6-12oC. Northeasterly winds 15-30 km/hr. After that, temperature \n" +
                "  increases. Northeasterly winds 15-30 km/hr. "
        ))
        val rows = (fts4VirtualModelAdapter.insert(
            fts4VirtualModelAdapter.docId(),
            Fts4VirtualModel_Table.name
        ) select fts4ContentModelAdapter.select(Fts4ContentModel_Table.id, Fts4ContentModel_Table.name))
            .execute()
        assert(rows > 0)
        val value =
            (fts4VirtualModelAdapter.select(
                resultFactory = StringResultFactory,
                snippet<Fts4VirtualModel>(
                    start = "[",
                    end = "]",
                    ellipses = "...",
                )
            ) where (fts4VirtualModelAdapter.tableNameLiteral() match "\"min* tem*\""))
                .execute()
        assertNotNull(value)
        assertEquals(
            value.value, "...the upper portion, [minimum] [temperature] 14-16oC \n" +
            "  and cool elsewhere, [minimum] [temperature] 17-20oC. Cold..."
        )
    }
}
