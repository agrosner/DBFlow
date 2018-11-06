package com.dbflow5.database.transaction

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.coroutines.awaitDelete
import com.dbflow5.coroutines.awaitInsert
import com.dbflow5.coroutines.awaitSave
import com.dbflow5.coroutines.awaitTransact
import com.dbflow5.coroutines.awaitUpdate
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.models.TwoColumnModel
import com.dbflow5.models.TwoColumnModel_Table
import com.dbflow5.query.delete
import com.dbflow5.query.select
import com.dbflow5.structure.save
import kotlinx.coroutines.runBlocking
import org.junit.Test

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
            database<TestDatabase> { db ->
                val simpleModel = SimpleModel("Name")
                val result = simpleModel.awaitSave(db)
                assert(result)

                assert(simpleModel.awaitDelete(db))
            }
        }
    }

    @Test
    fun testAwaitInsertAndDelete() {
        runBlocking {
            database<TestDatabase> { db ->
                val simpleModel = SimpleModel("Name")
                val result = simpleModel.awaitInsert(db)
                assert(result > 0)
                assert(simpleModel.awaitDelete(db))
            }
        }
    }

    @Test
    fun testAwaitUpdate() {
        runBlocking {
            database<TestDatabase> { db ->
                val simpleModel = TwoColumnModel(name = "Name", id = 5)
                val result = simpleModel.awaitSave(db)
                assert(result)

                simpleModel.id = 5
                val updated = simpleModel.awaitUpdate(db)
                assert(updated)

                val loadedModel = (select from TwoColumnModel::class
                        where TwoColumnModel_Table.id.eq(5))
                        .awaitTransact(db) { querySingle(it) }
                assert(loadedModel?.id == 5)
            }
        }
    }
}