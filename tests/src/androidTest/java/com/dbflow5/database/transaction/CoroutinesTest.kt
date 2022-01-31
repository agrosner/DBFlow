package com.dbflow5.database.transaction

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.coroutines.awaitDelete
import com.dbflow5.coroutines.awaitInsert
import com.dbflow5.coroutines.awaitSave
import com.dbflow5.coroutines.awaitTransact
import com.dbflow5.coroutines.awaitUpdate
import com.dbflow5.coroutines.toFlow
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.models.TwoColumnModel
import com.dbflow5.models.TwoColumnModel_Table
import com.dbflow5.query.delete
import com.dbflow5.query.select
import com.dbflow5.simpleModel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Description:
 */
class CoroutinesTest : BaseUnitTest() {

    @Test
    fun testTransact() {
        runBlocking {
            database<TestDatabase> {
                (0..9).forEach {
                    simpleModel.save(SimpleModel("$it"), db)
                }

                val query = (select from SimpleModel::class
                    where SimpleModel_Table.name.eq("5"))
                    .awaitTransact(db) { queryList(it) }

                assert(query.size == 1)


                val result = (delete<SimpleModel>()
                    where SimpleModel_Table.name.eq("5"))
                    .awaitTransact(db) { executeUpdateDelete(it) }
                assert(result == 1L)
            }
        }
    }

    @Test
    fun testAwaitSaveAndDelete() {
        runBlocking {
            database<TestDatabase> {
                val simpleModel = SimpleModel("Name")
                val result = simpleModel.awaitSave(db)
                assert(result.isSuccess)

                assert(simpleModel.awaitDelete(db).isSuccess)
            }
        }
    }

    @Test
    fun testAwaitInsertAndDelete() {
        runBlocking {
            database<TestDatabase> {
                val simpleModel = SimpleModel("Name")
                val result = simpleModel.awaitInsert(db)
                assert(result.isSuccess)
                assert(simpleModel.awaitDelete(db).isSuccess)
            }
        }
    }

    @Test
    fun testAwaitUpdate() {
        runBlocking {
            database<TestDatabase> {
                val simpleModel = TwoColumnModel(name = "Name", id = 5)
                val result = simpleModel.awaitSave(db)
                assert(result.isSuccess)

                simpleModel.id = 5
                val updated = simpleModel.awaitUpdate(db)
                assert(updated.isSuccess)

                val loadedModel = (select from TwoColumnModel::class
                    where TwoColumnModel_Table.id.eq(5))
                    .awaitTransact(db) { querySingle(it) }
                assert(loadedModel?.id == 5)
            }
        }
    }

    @Test
    fun testRetrievalFlow() = runBlockingTest {
        database<TestDatabase> {
            val simpleModel = TwoColumnModel(name = "Name", id = 5)
            val saveResult = simpleModel.awaitSave(db)
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
        database<TestDatabase> {
            val simpleModel = TwoColumnModel(name = "Name", id = 5)
            val result = simpleModel.awaitSave(db)
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