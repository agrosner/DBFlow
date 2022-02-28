package com.dbflow5.database.transaction

import app.cash.turbine.test
import com.dbflow5.TestDatabase_Database
import com.dbflow5.adapter.toFlow
import com.dbflow5.models.TwoColumnModel
import com.dbflow5.models.TwoColumnModel_Table
import com.dbflow5.query.select
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.twoColumnModelAdapter
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Description:
 */
class CoroutinesTest {

    @get:Rule
    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun testRetrievalFlow() = dbRule.runTest {
        (twoColumnModelAdapter.select() where TwoColumnModel_Table.id.eq(5))
            .toFlow(db, runQueryOnCollect = false) { single() }
            .test {
                val simpleModel = TwoColumnModel(name = "Name", id = 5)
                val saveResult = twoColumnModelAdapter.save(simpleModel)
                assertEquals(saveResult.id, 5)
                assertEquals(saveResult, awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
    }

    @Test
    fun testObservingTableChanges() = dbRule.runTest {
        twoColumnModelAdapter.select()
            .toFlow(db) { list() }
            .test {
                // first item subscription
                awaitItem()

                val simpleModel = TwoColumnModel(name = "Name", id = 5)
                val result = twoColumnModelAdapter.save(simpleModel)
                assertEquals(result.id, 5)
                assertEquals(listOf(result), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
    }
}