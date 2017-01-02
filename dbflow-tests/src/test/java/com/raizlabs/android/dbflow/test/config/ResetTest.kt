package com.raizlabs.android.dbflow.test.config

import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.test.FlowTestCase
import com.raizlabs.android.dbflow.test.TestDatabase
import com.raizlabs.android.dbflow.test.sql.unique.ResetModel
import junit.framework.Assert.assertEquals
import org.junit.Test

class ResetTest : FlowTestCase() {

    @Test
    fun test_resetSingleDatabase() {

        var aModel = ResetModel()
        aModel.name = "bob"
        aModel.insert()
        assertEquals(1, SQLite.select().from(ResetModel::class.java).count())


        FlowManager.getDatabase(TestDatabase::class.java).reset(context)
        assertEquals(0, SQLite.select().from(ResetModel::class.java).count())

        aModel = ResetModel()
        aModel.name = "fred"
        aModel.insert()
        assertEquals(1, SQLite.select().from(ResetModel::class.java).count())

    }
}
