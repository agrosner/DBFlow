package com.raizlabs.dbflow5.dbflow.sql.language

import com.raizlabs.dbflow5.dbflow.BaseUnitTest
import com.raizlabs.dbflow5.annotation.ConflictAction
import com.raizlabs.dbflow5.dbflow.assertEquals
import com.raizlabs.dbflow5.config.databaseForTable
import com.raizlabs.dbflow5.dbflow.models.NumberModel
import com.raizlabs.dbflow5.dbflow.models.NumberModel_Table.id
import com.raizlabs.dbflow5.dbflow.models.SimpleModel
import com.raizlabs.dbflow5.dbflow.models.SimpleModel_Table.name
import com.raizlabs.dbflow5.query.property.Property
import com.raizlabs.dbflow5.query.set
import com.raizlabs.dbflow5.query.update
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