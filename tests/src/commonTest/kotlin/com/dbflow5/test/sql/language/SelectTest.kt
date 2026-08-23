package com.dbflow5.test.sql.language

import com.dbflow5.query.`as`
import com.dbflow5.query.innerJoin
import com.dbflow5.query.select
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.test.SimpleModel
import com.dbflow5.test.TestDatabase_Database
import com.dbflow5.test.TestRule
import com.dbflow5.test.TwoColumnModel
import com.dbflow5.test.assertEquals
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SelectTest : TestRule() {


    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun validateSelect() {
        dbRule {
            "SELECT `name`, `id` FROM `TwoColumnModel`".assertEquals(
                twoColumnModelAdapter.select(
                    TwoColumnModel.name,
                    TwoColumnModel.id
                )
            )
        }
    }

    @Test
    fun validateSelectDistinct() {
        dbRule {
            "SELECT DISTINCT `name` FROM `SimpleModel`".assertEquals(
                simpleModelAdapter.select(TwoColumnModel.name).distinct()
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
            assertEquals(
                expected,
                (select from SimpleModel).query.trim()
            )
        }
    }

    @Test
    fun validateProjectionFrom() {
        dbRule {
            assertEquals(
                "SELECT `name` FROM `SimpleModel`",
                simpleModelAdapter.select(SimpleModel.name).query.trim()
            )
        }
    }

    @Test
    fun validateMultipleProjection() {
        dbRule {
            assertEquals(
                "SELECT `name`, `name`, `id` FROM `SimpleModel`",
                simpleModelAdapter.select(
                    SimpleModel.name,
                    TwoColumnModel.name,
                    TwoColumnModel.id
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
                    on SimpleModel.name.eq(TwoColumnModel.name.withTable())
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