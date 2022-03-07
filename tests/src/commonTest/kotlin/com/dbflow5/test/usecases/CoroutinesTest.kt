package com.dbflow5.test.usecases

import app.cash.turbine.test
import com.dbflow5.adapter.toFlow
import com.dbflow5.query.select
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.test.TestDatabase_Database
import com.dbflow5.test.TwoColumnModel
import com.dbflow5.test.TwoColumnModel_Table
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Description:
 */
class CoroutinesTest {

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