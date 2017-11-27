package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.annotation.ConflictAction
import com.raizlabs.android.dbflow.assertEquals
import com.raizlabs.android.dbflow.config.databaseForTable
import com.raizlabs.android.dbflow.models.NumberModel
import com.raizlabs.android.dbflow.models.NumberModel_Table.id
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.models.SimpleModel_Table.name
import com.raizlabs.android.dbflow.query.set
import com.raizlabs.android.dbflow.query.update
import com.raizlabs.android.dbflow.query.property.Property
import org.junit.Test

class UpdateTest : BaseUnitTest() {

    @Test
    fun validateUpdateRollback() {
        databaseForTable<SimpleModel> {
            assertEquals("UPDATE OR ROLLBACK `SimpleModel`", update<SimpleModel>().orRollback())
        }
    }

    @Test
    fun validateUpdateFail() {
        databaseForTable<SimpleModel> {
            assertEquals("UPDATE OR FAIL `SimpleModel`", update<SimpleModel>().orFail())
        }
    }

    @Test
    fun validateUpdateIgnore() {
        databaseForTable<SimpleModel> {
            assertEquals("UPDATE OR IGNORE `SimpleModel`", update<SimpleModel>().orIgnore())
        }
    }

    @Test
    fun validateUpdateReplace() {
        databaseForTable<SimpleModel> {
            assertEquals("UPDATE OR REPLACE `SimpleModel`", update<SimpleModel>().orReplace())
        }
    }

    @Test
    fun validateUpdateAbort() {
        databaseForTable<SimpleModel> {
            assertEquals("UPDATE OR ABORT `SimpleModel`", update<SimpleModel>().orAbort())
        }
    }

    @Test
    fun validateSetQuery() {
        databaseForTable<SimpleModel> {
            assertEquals("UPDATE `SimpleModel` SET `name`='name'", update<SimpleModel>() set (name eq "name"))
        }
    }

    @Test
    fun validateWildcardQuery() {
        databaseForTable<SimpleModel> {
            assertEquals("UPDATE OR FAIL `NumberModel` SET `id`=? WHERE `id`=?",
                    update<NumberModel>().or(ConflictAction.FAIL)
                            .set(id.eq(Property.WILDCARD))
                            .where(id.eq(Property.WILDCARD)))
        }
    }
}