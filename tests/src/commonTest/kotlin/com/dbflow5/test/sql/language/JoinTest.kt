package com.dbflow5.test.sql.language

import com.dbflow5.query.crossJoin
import com.dbflow5.query.innerJoin
import com.dbflow5.query.leftOuterJoin
import com.dbflow5.query.naturalJoin
import com.dbflow5.query.select
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.test.SimpleModel
import com.dbflow5.test.TestDatabase_Database
import com.dbflow5.test.TestRule
import com.dbflow5.test.TwoColumnModel
import kotlin.test.Test
import kotlin.test.assertEquals


class JoinTest : TestRule() {

    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun validateAliasJoin() {
        dbRule {
            assertEquals(
                "SELECT * FROM `SimpleModel` " +
                    "INNER JOIN `TwoColumnModel` AS `Name` " +
                    "ON `TwoColumnModel`.`name` = `name`",
                ((db.simpleModelAdapter.select() innerJoin
                    db.twoColumnModelAdapter).`as`("Name") on TwoColumnModel.name.withTable()
                    .eq(SimpleModel.name)).query.trim()
            )
        }
    }

    @Test
    fun testInnerJoin() {
        dbRule {
            val join = simpleModelAdapter.select() innerJoin
                twoColumnModelAdapter on TwoColumnModel.name.withTable()
                .eq(SimpleModel.name)
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
        dbRule {
            val join = simpleModelAdapter.select() leftOuterJoin
                twoColumnModelAdapter on TwoColumnModel.name.withTable()
                .eq(SimpleModel.name)
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
        dbRule {
            val join = simpleModelAdapter.select() crossJoin
                db.twoColumnModelAdapter on TwoColumnModel.name.withTable()
                .eq(SimpleModel.name)
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
        dbRule {
            val join = simpleModelAdapter.select() innerJoin
                twoColumnModelAdapter on TwoColumnModel.name.withTable()
                .eq(SimpleModel.name) crossJoin
                twoColumnModelAdapter on TwoColumnModel.id.withTable()
                .eq(SimpleModel.name)
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
        dbRule {
            val join = simpleModelAdapter.select() innerJoin
                twoColumnModelAdapter using SimpleModel.name.withTable()
            assertEquals(
                "SELECT * FROM `SimpleModel` INNER JOIN `TwoColumnModel` USING (`SimpleModel`.`name`)",
                join.query.trim()
            )
        }
    }

    @Test
    fun testNaturalJoin() {
        dbRule {
            val join = (simpleModelAdapter.select() naturalJoin twoColumnModelAdapter)
            assertEquals(
                "SELECT * FROM `SimpleModel` NATURAL JOIN `TwoColumnModel`",
                join.query.trim()
            )
        }
    }
}