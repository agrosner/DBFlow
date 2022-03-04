package com.dbflow5.test.sql.language

import com.dbflow5.test.TestDatabase_Database
import com.dbflow5.annotation.ConflictAction
import com.dbflow5.assertEquals
import com.dbflow5.models.NumberModel_Table
import com.dbflow5.test.SimpleModel_Table
import com.dbflow5.numberModelAdapter
import com.dbflow5.query.operations.Literal
import com.dbflow5.query.orAbort
import com.dbflow5.query.orFail
import com.dbflow5.query.orIgnore
import com.dbflow5.query.orReplace
import com.dbflow5.query.orRollback
import com.dbflow5.query.update
import com.dbflow5.test.simpleModelAdapter
import com.dbflow5.test.DatabaseTestRule
import org.junit.Rule
import kotlin.test.Test

class UpdateTest {

    
    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun validateUpdateRollback() {
        dbRule {
            "UPDATE OR ROLLBACK `SimpleModel`".assertEquals(
                simpleModelAdapter.update().orRollback()
            )
        }
    }

    @Test
    fun validateUpdateFail() {
        dbRule {
            "UPDATE OR FAIL `SimpleModel`".assertEquals(
                simpleModelAdapter.update().orFail()
            )
        }
    }

    @Test
    fun validateUpdateIgnore() {
        dbRule {
            "UPDATE OR IGNORE `SimpleModel`".assertEquals(
                simpleModelAdapter.update().orIgnore()
            )
        }
    }

    @Test
    fun validateUpdateReplace() {
        dbRule {
            "UPDATE OR REPLACE `SimpleModel`".assertEquals(
                simpleModelAdapter.update().orReplace()
            )
        }
    }

    @Test
    fun validateUpdateAbort() {
        dbRule {
            "UPDATE OR ABORT `SimpleModel`".assertEquals(
                simpleModelAdapter.update().orAbort()
            )
        }
    }

    @Test
    fun validateSetQuery() {
        dbRule {
            "UPDATE `SimpleModel` SET `name` = 'name'".assertEquals(
                simpleModelAdapter.update() set (SimpleModel_Table.name eq "name")
            )
        }
    }

    @Test
    fun validateWildcardQuery() {
        dbRule {
            "UPDATE OR FAIL `NumberModel` SET `id` = ? WHERE `id` = ?".assertEquals(
                numberModelAdapter.update().or(ConflictAction.FAIL)
                    .set(NumberModel_Table.id.eq(Literal.WildCard))
                    .where(NumberModel_Table.id.eq(Literal.WildCard))
            )
        }
    }
}