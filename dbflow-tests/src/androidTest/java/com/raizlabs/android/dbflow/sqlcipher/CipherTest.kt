package com.raizlabs.android.dbflow.sqlcipher

import com.raizlabs.android.dbflow.BaseInstrumentedUnitTest
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.result
import com.raizlabs.android.dbflow.kotlinextensions.select
import com.raizlabs.android.dbflow.kotlinextensions.where
import com.raizlabs.android.dbflow.sql.language.Delete
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