package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.models.TwoColumnModel_Table
import com.dbflow5.query.crossJoin
import com.dbflow5.query.innerJoin
import com.dbflow5.query.leftOuterJoin
import com.dbflow5.query.naturalJoin
import com.dbflow5.query.select
import com.dbflow5.simpleModelAdapter
import com.dbflow5.twoColumnModelAdapter
import org.junit.Test
import kotlin.test.assertEquals


class JoinTest : BaseUnitTest() {

    @Test
    fun validateAliasJoin() {
        database<TestDatabase> {
            assertEquals(
                "SELECT * FROM `SimpleModel` " +
                    "INNER JOIN `TwoColumnModel` AS `Name` " +
                    "ON `TwoColumnModel`.`name` = `name`",
                ((db.simpleModelAdapter.select() innerJoin
                    db.twoColumnModelAdapter).`as`("Name") on TwoColumnModel_Table.name.withTable()
                    .eq(SimpleModel_Table.name)).query.trim()
            )
        }
    }

    @Test
    fun testInnerJoin() {
        database<TestDatabase> {
            val join = simpleModelAdapter.select() innerJoin
                twoColumnModelAdapter on TwoColumnModel_Table.name.withTable()
                .eq(SimpleModel_Table.name)
            assertEquals(
                "SELECT * FROM `SimpleModel` " +
                    "INNER JOIN `TwoColumnModel` " +
                    "ON `TwoColumnModel`.`name` = `name`",
                join.query.trim()
            )
        }
    }

    @Test
    fun testLeftOuterJoin() {
        database<TestDatabase> {
            val join = simpleModelAdapter.select() leftOuterJoin
                twoColumnModelAdapter on TwoColumnModel_Table.name.withTable()
                .eq(SimpleModel_Table.name)
            assertEquals(
                "SELECT * FROM `SimpleModel` " +
                    "LEFT OUTER JOIN `TwoColumnModel` " +
                    "ON `TwoColumnModel`.`name` = `name`",
                join.query.trim()
            )
        }
    }

    @Test
    fun testCrossJoin() {
        database<TestDatabase> {
            val join = simpleModelAdapter.select() crossJoin
                db.twoColumnModelAdapter on TwoColumnModel_Table.name.withTable()
                .eq(SimpleModel_Table.name)
            assertEquals(
                "SELECT * FROM `SimpleModel` " +
                    "CROSS JOIN `TwoColumnModel` " +
                    "ON `TwoColumnModel`.`name` = `name`",
                join.query.trim()
            )
        }
    }

    @Test
    fun testMultiJoin() {
        database<TestDatabase> {
            val join = simpleModelAdapter.select() innerJoin
                twoColumnModelAdapter on TwoColumnModel_Table.name.withTable()
                .eq(SimpleModel_Table.name) crossJoin
                twoColumnModelAdapter on TwoColumnModel_Table.id.withTable()
                .eq(SimpleModel_Table.name)
            assertEquals(
                "SELECT * FROM `SimpleModel` " +
                    "INNER JOIN `TwoColumnModel` " +
                    "ON `TwoColumnModel`.`name` = `name` " +
                    "CROSS JOIN `TwoColumnModel` " +
                    "ON `TwoColumnModel`.`id` = `name`",
                join.query.trim()
            )
        }
    }

    @Test
    fun testInnerJoinOnUsing() {
        database<TestDatabase> {
            val join = simpleModelAdapter.select() innerJoin
                twoColumnModelAdapter using SimpleModel_Table.name.withTable()
            assertEquals(
                "SELECT * FROM `SimpleModel` INNER JOIN `TwoColumnModel` USING (`SimpleModel`.`name`)",
                join.query.trim()
            )
        }
    }

    @Test
    fun testNaturalJoin() {
        database<TestDatabase> {
            val join = (simpleModelAdapter.select() naturalJoin twoColumnModelAdapter)
            assertEquals(
                "SELECT * FROM `SimpleModel` NATURAL JOIN `TwoColumnModel`",
                join.query.trim()
            )
        }
    }
}