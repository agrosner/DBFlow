package com.dbflow5.sqlcipher

import com.dbflow5.BaseInstrumentedUnitTest
import com.dbflow5.config.database
import com.dbflow5.query.delete
import com.dbflow5.query.select
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Description: Ensures we can use SQLCipher
 */
class CipherTest : BaseInstrumentedUnitTest() {

    @Test
    fun testCipherModel() = database(CipherDatabase::class) { t ->
        (delete() from CipherModel::class).execute(t)
        val model = CipherModel(name = "name")
        model.save(t)
        assertTrue(model.exists(t))

        val retrieval = (select from CipherModel::class
            where CipherModel_Table.name.eq("name"))
            .querySingle(t)
        assertTrue(retrieval!!.id == model.id)
        (delete() from CipherModel::class).execute(t)
    }
}