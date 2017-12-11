package com.raizlabs.dbflow5.sql.language

import com.raizlabs.dbflow5.BaseUnitTest
import com.raizlabs.dbflow5.annotation.ConflictAction
import com.raizlabs.dbflow5.assertEquals
import com.raizlabs.dbflow5.config.databaseForTable
import com.raizlabs.dbflow5.models.NumberModel
import com.raizlabs.dbflow5.models.NumberModel_Table.id
import com.raizlabs.dbflow5.models.SimpleModel
import com.raizlabs.dbflow5.models.SimpleModel_Table.name
import com.raizlabs.dbflow5.query.property.Property
import com.raizlabs.dbflow5.query.set
import com.raizlabs.dbflow5.query.update
import org.junit.Test

class UpdateTest : BaseUnitTest() {

    @Test
    fun validateUpdateRollback() {
        databaseForTable<SimpleModel> {
            "UPDATE OR ROLLBACK `SimpleModel`".assertEquals(update<SimpleModel>().orRollback())
        }
    }

    @Test
    fun validateUpdateFail() {
        databaseForTable<SimpleModel> {
            "UPDATE OR FAIL `SimpleModel`".assertEquals(update<SimpleModel>().orFail())
        }
    }

    @Test
    fun validateUpdateIgnore() {
        databaseForTable<SimpleModel> {
            "UPDATE OR IGNORE `SimpleModel`".assertEquals(update<SimpleModel>().orIgnore())
        }
    }

    @Test
    fun validateUpdateReplace() {
        databaseForTable<SimpleModel> {
            "UPDATE OR REPLACE `SimpleModel`".assertEquals(update<SimpleModel>().orReplace())
        }
    }

    @Test
    fun validateUpdateAbort() {
        databaseForTable<SimpleModel> {
            "UPDATE OR ABORT `SimpleModel`".assertEquals(update<SimpleModel>().orAbort())
        }
    }

    @Test
    fun validateSetQuery() {
        databaseForTable<SimpleModel> {
            "UPDATE `SimpleModel` SET `name`='name'".assertEquals(update<SimpleModel>() set (name eq "name"))
        }
    }

    @Test
    fun validateWildcardQuery() {
        databaseForTable<SimpleModel> {
            "UPDATE OR FAIL `NumberModel` SET `id`=? WHERE `id`=?".assertEquals(update<NumberModel>().or(ConflictAction.FAIL)
                .set(id.eq(Property.WILDCARD))
                .where(id.eq(Property.WILDCARD)))
        }
    }
}