package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.annotation.ConflictAction
import com.raizlabs.android.dbflow.assertEquals
import com.raizlabs.android.dbflow.config.writableDatabaseForTable
import com.raizlabs.android.dbflow.models.NumberModel
import com.raizlabs.android.dbflow.models.NumberModel_Table.id
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.models.SimpleModel_Table.name
import com.raizlabs.android.dbflow.sql.language.property.Property
import com.raizlabs.android.dbflow.sql.language.property.eq
import org.junit.Test

class UpdateTest : BaseUnitTest() {

    @Test
    fun validateUpdateRollback() = writableDatabaseForTable<SimpleModel>{
        assertEquals("UPDATE OR ROLLBACK `SimpleModel`", update<SimpleModel>().orRollback())
    }

    @Test
    fun validateUpdateFail()= writableDatabaseForTable<SimpleModel> {
        assertEquals("UPDATE OR FAIL `SimpleModel`", update<SimpleModel>().orFail())
    }

    @Test
    fun validateUpdateIgnore()= writableDatabaseForTable<SimpleModel> {
        assertEquals("UPDATE OR IGNORE `SimpleModel`", update<SimpleModel>().orIgnore())
    }

    @Test
    fun validateUpdateReplace()= writableDatabaseForTable<SimpleModel> {
        assertEquals("UPDATE OR REPLACE `SimpleModel`", update<SimpleModel>().orReplace())
    }

    @Test
    fun validateUpdateAbort() = writableDatabaseForTable<SimpleModel>{
        assertEquals("UPDATE OR ABORT `SimpleModel`", update<SimpleModel>().orAbort())
    }

    @Test
    fun validateSetQuery() = writableDatabaseForTable<SimpleModel>{
        assertEquals("UPDATE `SimpleModel` SET `name`='name'", update<SimpleModel>() set (name eq "name"))
    }

    @Test
    fun validateWildcardQuery() = writableDatabaseForTable<SimpleModel>{
        assertEquals("UPDATE OR FAIL `NumberModel` SET `id`=? WHERE `id`=?",
                update<NumberModel>().or(ConflictAction.FAIL)
                        .set(id.eq(Property.WILDCARD))
                        .where(id.eq(Property.WILDCARD)))
    }
}