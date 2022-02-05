package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.assertEquals
import com.dbflow5.config.database
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.models.TwoColumnModel_Table
import com.dbflow5.query2.innerJoin
import com.dbflow5.query2.select
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SelectTest : BaseUnitTest() {

    @Test
    fun validateSelect() {
        database<TestDatabase> { db ->
            "SELECT `name`, `id` FROM `TwoColumnModel`".assertEquals(
                db.twoColumnModelAdapter.select(
                    TwoColumnModel_Table.name,
                    TwoColumnModel_Table.id
                )
            )
        }
    }

    @Test
    fun validateSelectDistinct() {
        database<TestDatabase> { db ->
            "SELECT DISTINCT `name` FROM `SimpleModel`".assertEquals(
                db.simpleModelAdapter.select(TwoColumnModel_Table.name).distinct()
            )
        }
    }

    @Test
    fun validateSimpleSelect() {
        database<TestDatabase> { db ->
            assertEquals(
                "SELECT * FROM `SimpleModel`",
                db.simpleModelAdapter.select().query.trim()
            )
        }
    }

    @Test
    fun validateProjectionFrom() {
        database<TestDatabase> { db ->
            assertEquals(
                "SELECT `name` FROM `SimpleModel`",
                db.simpleModelAdapter.select(SimpleModel_Table.name).query.trim()
            )
        }
    }

    @Test
    fun validateMultipleProjection() {
        database<TestDatabase> { db ->
            assertEquals(
                "SELECT `name`, `name`, `id` FROM `SimpleModel`",
                db.simpleModelAdapter.select(
                    SimpleModel_Table.name,
                    TwoColumnModel_Table.name,
                    TwoColumnModel_Table.id
                ).query.trim()
            )
        }
    }

    @Test
    fun validateAlias() {
        database<TestDatabase> { db ->
            assertEquals(
                "SELECT * FROM `SimpleModel` AS `Simple`",
                (db.simpleModelAdapter.select() `as` "Simple").query.trim()
            )
        }
    }

    @Test
    fun validateJoins() {
        val database = database<TestDatabase>()

        val from = (
            database.simpleModelAdapter.select()
                innerJoin database.twoColumnModelAdapter
                on SimpleModel_Table.name.eq(TwoColumnModel_Table.name.withTable())
            )
        assertEquals(
            "SELECT * FROM `SimpleModel` " +
                "INNER JOIN `TwoColumnModel` " +
                "ON `name`=`TwoColumnModel`.`name`",
            from.query.trim()
        )
        assertTrue(from.associatedAdapters.isNotEmpty())
    }
}