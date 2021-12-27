package com.dbflow5.sqlcipher

import com.dbflow5.DBFlowInstrumentedTestRule
import com.dbflow5.DemoApp
import com.dbflow5.config.database
import com.dbflow5.query.delete
import com.dbflow5.query.select
import com.dbflow5.structure.exists
import com.dbflow5.structure.save
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
    fun testCipherModel() {
        database<CipherDatabase> { db ->
            (delete() from CipherModel::class).execute(db)
            val model = CipherModel(name = "name")
                .save(db)
                .getOrThrow()
            assertTrue(model.exists(db))

            val retrieval = (select from CipherModel::class
                where CipherModel_Table.name.eq("name"))
                .querySingle(db)
            assertTrue(retrieval!!.id == model.id)
            (delete() from CipherModel::class).execute(db)
        }
    }
}