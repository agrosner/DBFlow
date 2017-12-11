package com.raizlabs.dbflow5.database.transaction

import com.raizlabs.dbflow5.BaseUnitTest
import com.raizlabs.dbflow5.TestDatabase
import com.raizlabs.dbflow5.config.database
import com.raizlabs.dbflow5.models.SimpleModel
import com.raizlabs.dbflow5.models.SimpleModel_Table
import com.raizlabs.dbflow5.query.delete
import com.raizlabs.dbflow5.structure.save
import com.raizlabs.dbflow5.coroutines.awaitDelete
import com.raizlabs.dbflow5.coroutines.awaitInsert
import com.raizlabs.dbflow5.coroutines.awaitSave
import com.raizlabs.dbflow5.coroutines.awaitUpdate
import com.raizlabs.dbflow5.coroutines.awaitTransact
import com.raizlabs.dbflow5.query.list
import com.raizlabs.dbflow5.query.select
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test

/**
 * Description:
 */
class CoroutinesTest : BaseUnitTest() {

    @Test
    fun testTransact() {
        runBlocking {
            database<TestDatabase> {
                (0..9).forEach {
                    SimpleModel("$it").save()
                }

                val query = awaitTransact(
                        select from SimpleModel::class
                                where SimpleModel_Table.name.eq("5")) { list }

                assert(query.size == 1)


                val result = awaitTransact(
                        delete<SimpleModel>()
                                where SimpleModel_Table.name.eq("5")) { executeUpdateDelete() }
                assert(result == 1L)
            }
        }
    }

    @Test
    fun testAwaitSaveAndDelete() {
        runBlocking {
            database<TestDatabase> {
                val simpleModel = SimpleModel("Name")
                val result = simpleModel.awaitSave(this)
                assert(result)

                assert(simpleModel.awaitDelete(this))
            }
        }
    }

    @Test
    fun testAwaitInsertAndDelete() {
        runBlocking {
            database<TestDatabase> {
                val simpleModel = SimpleModel("Name")
                val result = simpleModel.awaitInsert(this)
                assert(result > 0)
                assert(simpleModel.awaitDelete(this))
            }
        }
    }

    @Test
    fun testAwaitUpdate() {
        runBlocking {
            database<TestDatabase> {
                val simpleModel = SimpleModel("Name")
                val result = simpleModel.awaitSave(this)
                assert(result)

                simpleModel.name = "NewName"
                assert(simpleModel.awaitUpdate(this))

                val loadedModel = awaitTransact(select from SimpleModel::class
                        where SimpleModel_Table.name.eq("NewName")) { querySingle() }
                assert(loadedModel?.name == "NewName")
            }
        }
    }
}