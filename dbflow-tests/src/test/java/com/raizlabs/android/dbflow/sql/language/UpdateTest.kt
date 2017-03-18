package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.SimpleModel
import com.raizlabs.android.dbflow.SimpleModel_Table.name
import com.raizlabs.android.dbflow.assertEquals
import com.raizlabs.android.dbflow.kotlinextensions.eq
import com.raizlabs.android.dbflow.kotlinextensions.set
import com.raizlabs.android.dbflow.kotlinextensions.update
import org.junit.Test

class UpdateTest : BaseUnitTest() {

    @Test
    fun validateUpdateRollback() {
        assertEquals("UPDATE OR ROLLBACK `SimpleModel`", update<SimpleModel>().orRollback())
    }

    @Test
    fun validateUpdateFail() {
        assertEquals("UPDATE OR FAIL `SimpleModel`", update<SimpleModel>().orFail())
    }

    @Test
    fun validateUpdateIgnore() {
        assertEquals("UPDATE OR IGNORE `SimpleModel`", update<SimpleModel>().orIgnore())
    }

    @Test
    fun validateUpdateReplace() {
        assertEquals("UPDATE OR REPLACE `SimpleModel`", update<SimpleModel>().orReplace())
    }

    @Test
    fun validateUpdateAbort() {
        assertEquals("UPDATE OR ABORT `SimpleModel`", update<SimpleModel>().orAbort())
    }

    @Test
    fun validateSetQuery() {
        assertEquals("UPDATE `SimpleModel` SET `name`='name'", update<SimpleModel>() set (name eq "name"))
    }
}