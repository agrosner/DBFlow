package com.dbflow5.database.transaction

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.config.readableTransaction
import com.dbflow5.config.writableTransaction
import com.dbflow5.coroutines.toFlow
import com.dbflow5.models.TwoColumnModel
import com.dbflow5.models.TwoColumnModel_Table
import com.dbflow5.query.select
import com.dbflow5.twoColumnModelAdapter
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Description:
 */
class CoroutinesTest : BaseUnitTest() {

    @Test
    fun testRetrievalFlow() = runBlockingTest {
        val database = database<TestDatabase>()
        database.writableTransaction {
            val simpleModel = TwoColumnModel(name = "Name", id = 5)
            val saveResult = twoColumnModelAdapter.save(simpleModel)
            assertEquals(saveResult.id, 5)
        }

        database.readableTransaction {
            (twoColumnModelAdapter.select() where TwoColumnModel_Table.id.eq(5))
                .toFlow(database) { single() }.first()
        }
    }

    @Test
    fun testObservingTableChanges() = runBlockingTest {
        val count = ConflatedBroadcastChannel(0)
        val database = database<TestDatabase>()
        val job = launch {
            database.readableTransaction {
                twoColumnModelAdapter.select()
                    .toFlow(database) { list() }
                    .collect {
                        count.offer(count.value + 1)
                    }
            }
        }
        database.writableTransaction {
            val simpleModel = TwoColumnModel(name = "Name", id = 5)
            val result = twoColumnModelAdapter.save(simpleModel)
            assertEquals(result.id, 5)
        }
        job.cancel()

        val value = count.value
        count.close()

        // last value
        // 1 emission
        assertEquals(1, value)
    }
}