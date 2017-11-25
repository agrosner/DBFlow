package com.raizlabs.android.dbflow.database.transaction

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.config.database
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.models.SimpleModel_Table
import com.raizlabs.android.dbflow.sql.language.delete
import com.raizlabs.android.dbflow.sql.language.select
import com.raizlabs.android.dbflow.sql.language.where
import com.raizlabs.android.dbflow.sql.queriable.list
import com.raizlabs.android.dbflow.structure.database.transaction.awaitDelete
import com.raizlabs.android.dbflow.structure.database.transaction.awaitInsert
import com.raizlabs.android.dbflow.structure.database.transaction.awaitSave
import com.raizlabs.android.dbflow.structure.database.transaction.awaitUpdate
import com.raizlabs.android.dbflow.structure.database.transaction.transact
import com.raizlabs.android.dbflow.structure.save
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

                val query = transact(
                        select from SimpleModel::class
                                where SimpleModel_Table.name.eq("5")) { list }

                assert(query.size == 1)


                val result = transact(
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

                val loadedModel = transact(select from SimpleModel::class
                        where SimpleModel_Table.name.eq("NewName")) { querySingle() }!!
                assert(loadedModel.name == "NewName")
            }
        }
    }
}