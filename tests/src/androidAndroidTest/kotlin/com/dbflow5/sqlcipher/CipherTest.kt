package com.dbflow5.sqlcipher

import androidx.test.platform.app.InstrumentationRegistry
import com.dbflow5.query.delete
import com.dbflow5.query.select
import com.dbflow5.test.CipherDatabase_Database
import com.dbflow5.test.CipherModel
import com.dbflow5.test.CipherModel_Table
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.test.TestTransactionDispatcherFactory
import org.junit.Assert.assertTrue
import kotlin.test.Test

/**
 * Description: Ensures we can use SQLCipher
 */
class CipherTest {

    val cipherRule = DatabaseTestRule(CipherDatabase_Database) {
        copy(
            openHelperCreator = SQLCipherOpenHelper.createHelperCreator(
                InstrumentationRegistry.getInstrumentation().targetContext,
                "dbflow-rules"
            ),
            transactionDispatcherFactory = TestTransactionDispatcherFactory(),
        )
    }

    @Test
    fun testCipherModel() = cipherRule.runTest {
        cipherAdapter.delete().execute()
        val model =
            cipherAdapter.save(CipherModel(name = "name", id = 0))
        assertTrue(cipherAdapter.exists(model))

        val retrieval = (cipherAdapter.select()
            where CipherModel_Table.name.eq("name"))
            .single()
        assertTrue(retrieval.id == model.id)
        cipherAdapter.delete().execute()
    }
}
