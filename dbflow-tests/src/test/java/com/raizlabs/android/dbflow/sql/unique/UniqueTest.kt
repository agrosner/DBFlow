package com.raizlabs.android.dbflow.sql.unique

import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.FlowTestCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Description:
 */
class UniqueTest : FlowTestCase() {

    lateinit var uniqueModel: UniqueModel

    @Before
    fun createModel() {
        uniqueModel = UniqueModel()
        uniqueModel.uniqueName = "This is unique"
        uniqueModel.anotherUnique = "This should rollback"
        uniqueModel.sharedUnique = "This is unique to both combos"
    }

    @Test
    fun testUniqueReplacesExisting() {
        uniqueModel.anotherUnique = System.currentTimeMillis().toString() + ""// guarantee unique for this test.
        uniqueModel.insert()
        assertTrue(uniqueModel.exists())

        uniqueModel.insert()
        assertEquals(1, SQLite.selectCountOf().from(UniqueModel::class.java).count())
    }
}
