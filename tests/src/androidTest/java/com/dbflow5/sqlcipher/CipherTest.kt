package com.dbflow5.sqlcipher

import com.dbflow5.DemoApp
import com.dbflow5.test.TestTransactionDispatcherFactory
import com.dbflow5.query.delete
import com.dbflow5.query.select
import com.dbflow5.test.DatabaseTestRule
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Description: Ensures we can use SQLCipher
 */
class CipherTest {

    val cipherRule = DatabaseTestRule(CipherDatabase_Database) {
        copy(
            openHelperCreator = SQLCipherOpenHelper.createHelperCreator(
                DemoApp.context,
                "dbflow-rules"
            ),
            transactionDispatcherFactory = TestTransactionDispatcherFactory(),
        )
    }

    @Test
    fun testCipherModel() = cipherRule.runTest {
        cipherAdapter.delete().execute()
        val model =
            cipherAdapter.save(CipherModel(name = "name"))
        assertTrue(cipherAdapter.exists(model))

        val retrieval = (cipherAdapter.select()
            where CipherModel_Table.name.eq("name"))
            .single()
        assertTrue(retrieval.id == model.id)
        cipherAdapter.delete().execute()
    }
}
