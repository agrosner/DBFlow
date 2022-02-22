package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.assertEquals
import com.dbflow5.config.database
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.models.TwoColumnModel_Table
import com.dbflow5.query.`as`
import com.dbflow5.query.innerJoin
import com.dbflow5.query.select
import com.dbflow5.simpleModelAdapter
import com.dbflow5.twoColumnModelAdapter
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SelectTest : BaseUnitTest() {

    @Test
    fun validateSelect() {
        database<TestDatabase> {
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
        database<TestDatabase> {
            "SELECT DISTINCT `name` FROM `SimpleModel`".assertEquals(
                simpleModelAdapter.select(TwoColumnModel_Table.name).distinct()
            )
        }
    }

    @Test
    fun validateSimpleSelect() {
        database<TestDatabase> {
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
        database<TestDatabase> {
            assertEquals(
                "SELECT `name` FROM `SimpleModel`",
                simpleModelAdapter.select(SimpleModel_Table.name).query.trim()
            )
        }
    }

    @Test
    fun validateMultipleProjection() {
        database<TestDatabase> {
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
        database<TestDatabase> {
            assertEquals(
                "SELECT * FROM `SimpleModel` AS `Simple`",
                (simpleModelAdapter.select() `as` "Simple").query.trim()
            )
        }
    }

    @Test
    fun validateJoins() {
        database<TestDatabase> {
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