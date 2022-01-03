package com.dbflow5.database.transaction

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.models.TwoColumnModel
import com.dbflow5.models.TwoColumnModel_Table
import com.dbflow5.query.delete
import com.dbflow5.query.select
import com.dbflow5.structure.delete
import com.dbflow5.structure.insert
import com.dbflow5.structure.save
import com.dbflow5.structure.update
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
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
            database<TestDatabase> { db ->
                (0..9).forEach {
                    SimpleModel("$it").save(db)
                }

                val query = (select from SimpleModel::class
                    where SimpleModel_Table.name.eq("5"))
                    .queryList(db)

                assert(query.size == 1)

                val result = (delete<SimpleModel>()
                    where SimpleModel_Table.name.eq("5"))
                    .executeUpdateDelete(db)
                assert(result == 1L)
            }
        }
    }

    @Test
    fun testAwaitSaveAndDelete() {
        runBlocking {
            database<TestDatabase> { db ->
                val simpleModel = SimpleModel("Name")
                val result = simpleModel.save(db)
                assert(result.isSuccess)

                assert(simpleModel.delete(db).isSuccess)
            }
        }
    }

    @Test
    fun testAwaitInsertAndDelete() {
        runBlocking {
            database<TestDatabase> { db ->
                val simpleModel = SimpleModel("Name")
                val result = simpleModel.insert(db)
                assert(result.isSuccess)
                assert(simpleModel.delete(db).isSuccess)
            }
        }
    }

    @Test
    fun testAwaitUpdate() {
        runBlocking {
            database<TestDatabase> { db ->
                val simpleModel = TwoColumnModel(name = "Name", id = 5)
                val result = simpleModel.save(db)
                assert(result.isSuccess)

                simpleModel.id = 5
                val updated = simpleModel.update(db)
                assert(updated.isSuccess)

                val loadedModel = (select from TwoColumnModel::class
                    where TwoColumnModel_Table.id.eq(5))
                    .querySingle(db)
                assert(loadedModel?.id == 5)
            }
        }
    }

    @Test
    fun testRetrievalFlow() = runBlockingTest {
        database<TestDatabase> { db ->
            val simpleModel = TwoColumnModel(name = "Name", id = 5)
            val result = simpleModel.save(db)
            assert(result.isSuccess)

            val secondResult =
                (select from TwoColumnModel::class where TwoColumnModel_Table.id.eq(5))
                    .querySingle(db)
            assert(secondResult != null)
        }


    }

    @Test
    fun testObservingTableChanges() = runBlockingTest {
        val count = MutableStateFlow(0)
        val job = launch {
            (select from TwoColumnModel::class)
                .queryList(database<TestDatabase>())
                .asFlow()
                .collect {
                    count.emit(count.value + 1)
                }
        }
        database<TestDatabase> { db ->
            val simpleModel = TwoColumnModel(name = "Name", id = 5)
            val result = simpleModel.save(db)
            assert(result.isSuccess)
        }
        job.cancel()

        val value = count.value
        // last value
        // 1 emission
        assertEquals(1, value)
    }
}