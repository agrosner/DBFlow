package com.dbflow5.test.sql.language

import com.dbflow5.test.TestDatabase_Database
import com.dbflow5.assertEquals
import com.dbflow5.models.SimpleModel
import com.dbflow5.test.SimpleModel_Table
import com.dbflow5.test.TwoColumnModel_Table
import com.dbflow5.query.`as`
import com.dbflow5.query.innerJoin
import com.dbflow5.query.select
import com.dbflow5.test.simpleModelAdapter
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.test.twoColumnModelAdapter
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SelectTest {

    
    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun validateSelect() {
        dbRule {
            "SELECT `name`, `id` FROM `TwoColumnModel`".assertEquals(
                twoColumnModelAdapter.select(
                    TwoColumnModel_Table.name,
                    TwoColumnModel_Table.id
                )
            )
        }
    }

    @Test
    fun validateSelectDistinct() {
        dbRule {
            "SELECT DISTINCT `name` FROM `SimpleModel`".assertEquals(
                simpleModelAdapter.select(TwoColumnModel_Table.name).distinct()
            )
        }
    }

    @Test
    fun validateSimpleSelect() {
        dbRule {
            // compatibility notation
            val expected = "SELECT * FROM `SimpleModel`"
            assertEquals(
                expected,
                simpleModelAdapter.select().query.trim()
            )

            // compatibility notation
            assertEquals(
                expected,
                (select from simpleModelAdapter).query.trim()
            )
            // table compatibility
            assertEquals(
                expected,
                (select from SimpleModel::class).query.trim()
            )
        }
    }

    @Test
    fun validateProjectionFrom() {
        dbRule {
            assertEquals(
                "SELECT `name` FROM `SimpleModel`",
                simpleModelAdapter.select(SimpleModel_Table.name).query.trim()
            )
        }
    }

    @Test
    fun validateMultipleProjection() {
        dbRule {
            assertEquals(
                "SELECT `name`, `name`, `id` FROM `SimpleModel`",
                simpleModelAdapter.select(
                    SimpleModel_Table.name,
                    TwoColumnModel_Table.name,
                    TwoColumnModel_Table.id
                ).query.trim()
            )
        }
    }

    @Test
    fun validateAlias() {
        dbRule {
            assertEquals(
                "SELECT * FROM `SimpleModel` AS `Simple`",
                (simpleModelAdapter.select() `as` "Simple").query.trim()
            )
        }
    }

    @Test
    fun validateJoins() {
        dbRule {
            val from = (
                simpleModelAdapter.select()
                    innerJoin twoColumnModelAdapter
                    on SimpleModel_Table.name.eq(TwoColumnModel_Table.name.withTable())
                )
            assertEquals(
                "SELECT * FROM `SimpleModel` " +
                    "INNER JOIN `TwoColumnModel` " +
                    "ON `name` = `TwoColumnModel`.`name`",
                from.query.trim()
            )
            assertTrue(from.associatedAdapters.isNotEmpty())
        }
    }
}