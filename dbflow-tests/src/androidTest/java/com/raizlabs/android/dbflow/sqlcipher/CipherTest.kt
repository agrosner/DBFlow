package com.raizlabs.android.dbflow.sqlcipher

import com.raizlabs.android.dbflow.BaseInstrumentedUnitTest
import com.raizlabs.android.dbflow.config.database
import com.raizlabs.android.dbflow.sql.language.delete
import com.raizlabs.android.dbflow.sql.language.select
import com.raizlabs.android.dbflow.sql.language.where
import com.raizlabs.android.dbflow.sql.queriable.result
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