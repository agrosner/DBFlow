package com.raizlabs.dbflow5.sqlcipher

import com.raizlabs.dbflow5.BaseInstrumentedUnitTest
import com.raizlabs.dbflow5.config.database
import com.raizlabs.dbflow5.query.delete
import com.raizlabs.dbflow5.query.result
import com.raizlabs.dbflow5.query.select
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Description: Ensures we can use SQLCipher
 */
class CipherTest : BaseInstrumentedUnitTest() {

    @Test
    fun testCipherModel() = database(CipherDatabase::class) {
        (delete() from CipherModel::class).execute()
        val model = CipherModel(name = "name")
        model.save(this)
        assertTrue(model.exists(this))

        val retrieval = (select from CipherModel::class
            where CipherModel_Table.name.eq("name"))
            .result
        assertTrue(retrieval!!.id == model.id)
        (delete() from CipherModel::class).execute()
    }
}