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
import com.raizlabs.android.dbflow.structure.database.transaction.transact
import com.raizlabs.android.dbflow.structure.save
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Test

/**
 * Description:
 */
class CoroutinesTest : BaseUnitTest() {

    @Test
    fun testCanRunCoroutines() {
        runBlocking {
            database<TestDatabase> {
                (0..9).forEach {
                    SimpleModel("$it").save()
                }

                val query = (select from SimpleModel::class where SimpleModel_Table.name.eq("5"))
                        .transact(this) { list }

                assert(query.size == 1)


                val result = (delete<SimpleModel>() where SimpleModel_Table.name.eq("5"))
                        .transact(this) { executeUpdateDelete() }
                assert(result == 1L)
            }
        }
    }
}