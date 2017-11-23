package com.raizlabs.android.dbflow.sqlcipher

import com.raizlabs.android.dbflow.BaseInstrumentedUnitTest
import com.raizlabs.android.dbflow.sql.language.Delete
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
    fun testCipherModel() {
        Delete.table(CipherModel::class.java)
        val model = CipherModel(name = "name")
        model.save()

        assertTrue(model.exists())

        val retrieval = (select from CipherModel::class where CipherModel_Table.name.eq("name")).result
        assertTrue(retrieval!!.id == model.id)
        Delete.table(CipherModel::class.java)
    }
}