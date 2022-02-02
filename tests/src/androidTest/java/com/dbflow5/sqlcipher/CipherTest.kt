package com.dbflow5.sqlcipher

import com.dbflow5.DBFlowInstrumentedTestRule
import com.dbflow5.DemoApp
import com.dbflow5.config.database
import com.dbflow5.config.writableTransaction
import com.dbflow5.query.delete
import com.dbflow5.query.select
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Description: Ensures we can use SQLCipher
 */
class CipherTest {

    @JvmField
    @Rule
    var dblflowTestRule = DBFlowInstrumentedTestRule.create {
        database<CipherDatabase>(
            openHelperCreator = SQLCipherOpenHelper.createHelperCreator(
                DemoApp.context,
                "dbflow-rules"
            )
        )
    }

    @Test
    fun testCipherModel() = runBlockingTest {
        database<CipherDatabase>().writableTransaction {
            (delete() from CipherModel::class).execute()
            val model =
                cipherAdapter.save(CipherModel(name = "name"))
                    .getOrThrow()
            assertTrue(cipherAdapter.exists(model))

            val retrieval = (select from CipherModel::class
                where CipherModel_Table.name.eq("name"))
                .requireSingle()
            assertTrue(retrieval.id == model.id)
            (delete() from CipherModel::class).execute()
        }
    }
}