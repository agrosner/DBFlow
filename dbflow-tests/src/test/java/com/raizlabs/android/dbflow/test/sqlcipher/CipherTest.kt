package com.raizlabs.android.dbflow.test.sqlcipher

import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.test.FlowTestCase

import net.sqlcipher.database.SQLiteDatabase

import org.junit.Before
import org.junit.Test

import org.junit.Assert.assertTrue

/**
 * Description:
 */
class CipherTest : FlowTestCase() {


    @Test
    fun testCipherModel() {
        Delete.table(CipherModel::class.java)

        val model = CipherModel()
        model.name = "name"
        model.save()

        assertTrue(model.exists())

        val retrieval = SQLite.select()
                .from(CipherModel::class.java)
                .where(CipherModel_Table.name.eq("name")).querySingle()
        assertTrue(retrieval!!.id == model.id)

        Delete.table(CipherModel::class.java)
    }
}
