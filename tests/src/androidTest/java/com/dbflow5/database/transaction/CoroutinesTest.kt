package com.dbflow5.database.transaction

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.config.writableTransaction
import com.dbflow5.coroutines.toFlow
import com.dbflow5.models.TwoColumnModel
import com.dbflow5.models.TwoColumnModel_Table
import com.dbflow5.query.select
import com.dbflow5.twoColumnModel
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
        database<TestDatabase>().writableTransaction {
            val simpleModel = TwoColumnModel(name = "Name", id = 5)
            val saveResult = twoColumnModel.save(simpleModel)
            assert(saveResult.isSuccess)
            val result = (select from TwoColumnModel::class where TwoColumnModel_Table.id.eq(5))
                .toFlow(db) { querySingle(it) }.first()
            assert(result != null)
        }
    }

    @Test
    fun testObservingTableChanges() = runBlockingTest {
        val count = ConflatedBroadcastChannel(0)
        val job = launch {
            (select from TwoColumnModel::class)
                .toFlow(database<TestDatabase>()) { queryList(it) }
                .collect {
                    count.offer(count.value + 1)
                }
        }
        database<TestDatabase>().writableTransaction {
            val simpleModel = TwoColumnModel(name = "Name", id = 5)
            val result = twoColumnModel.save(simpleModel)
            assert(result.isSuccess)
        }
        job.cancel()

        val value = count.value
        count.close()

        // last value
        // 1 emission
        assertEquals(1, value)
    }
}