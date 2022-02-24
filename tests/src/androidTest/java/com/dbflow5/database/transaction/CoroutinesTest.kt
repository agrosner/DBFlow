package com.dbflow5.database.transaction

import com.dbflow5.TestDatabase_Database
import com.dbflow5.coroutines.toFlow
import com.dbflow5.models.TwoColumnModel
import com.dbflow5.models.TwoColumnModel_Table
import com.dbflow5.query.select
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.twoColumnModelAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Description:
 */
class CoroutinesTest {

    @get:Rule
    val dbRule = DatabaseTestRule(TestDatabase_Database::create)

    @Test
    fun testRetrievalFlow() = dbRule.runBlockingTest {
        val simpleModel = TwoColumnModel(name = "Name", id = 5)
        val saveResult = twoColumnModelAdapter.save(simpleModel)
        assertEquals(saveResult.id, 5)
        (twoColumnModelAdapter.select() where TwoColumnModel_Table.id.eq(5))
            .toFlow(db) { single() }.first()
    }

    @Test
    fun testObservingTableChanges() = dbRule.runBlockingTest {
        val count = MutableStateFlow(0)
        val job = launch {
            twoColumnModelAdapter.select()
                .toFlow(db) { list() }
                .collect {
                    count.emit(count.value + 1)
                }
        }
        val simpleModel = TwoColumnModel(name = "Name", id = 5)
        val result = twoColumnModelAdapter.save(simpleModel)
        assertEquals(result.id, 5)
        job.cancel()

        val value = count.value

        // last value
        // 1 emission
        assertEquals(1, value)
    }
}