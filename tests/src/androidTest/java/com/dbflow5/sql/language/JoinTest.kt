package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.models.TwoColumnModel_Table
import com.dbflow5.query.select
import org.junit.Assert.assertEquals
import org.junit.Test


class JoinTest : BaseUnitTest() {

    @Test
    fun validateAliasJoin() {
        database<TestDatabase> { db ->
            assertEquals(
                "SELECT * FROM `SimpleModel` INNER JOIN `TwoColumnModel` AS `Name` ON `TwoColumnModel`.`name`=`name`",
                ((select from db.simpleModelAdapter innerJoin
                    db.twoColumnModelAdapter).`as`("Name") on TwoColumnModel_Table.name.withTable()
                    .eq(SimpleModel_Table.name)).query.trim()
            )
        }
    }

    @Test
    fun testInnerJoin() {
        database<TestDatabase> { db ->
            val join = select from db.simpleModelAdapter innerJoin
                db.twoColumnModelAdapter on TwoColumnModel_Table.name.withTable()
                .eq(SimpleModel_Table.name)
            assertEquals(
                "SELECT * FROM `SimpleModel` INNER JOIN `TwoColumnModel` ON `TwoColumnModel`.`name`=`name`",
                join.query.trim()
            )
        }
    }

    @Test
    fun testLeftOuterJoin() {
        database<TestDatabase> { db ->
            val join = select from db.simpleModelAdapter leftOuterJoin
                db.twoColumnModelAdapter on TwoColumnModel_Table.name.withTable()
                .eq(SimpleModel_Table.name)
            assertEquals(
                "SELECT * FROM `SimpleModel` LEFT OUTER JOIN `TwoColumnModel` ON `TwoColumnModel`.`name`=`name`",
                join.query.trim()
            )
        }
    }

    @Test
    fun testCrossJoin() {
        database<TestDatabase> { db ->
            val join = select from db.simpleModelAdapter crossJoin
                db.twoColumnModelAdapter on TwoColumnModel_Table.name.withTable()
                .eq(SimpleModel_Table.name)
            assertEquals(
                "SELECT * FROM `SimpleModel` CROSS JOIN `TwoColumnModel` ON `TwoColumnModel`.`name`=`name`",
                join.query.trim()
            )
        }
    }

    @Test
    fun testMultiJoin() {
        database<TestDatabase> { db ->
            val join = select from db.simpleModelAdapter innerJoin
                db.twoColumnModelAdapter on TwoColumnModel_Table.name.withTable()
                .eq(SimpleModel_Table.name) crossJoin
                db.twoColumnModelAdapter on TwoColumnModel_Table.id.withTable()
                .eq(SimpleModel_Table.name)
            assertEquals(
                "SELECT * FROM `SimpleModel` INNER JOIN `TwoColumnModel` ON `TwoColumnModel`.`name`=`name`" +
                    " CROSS JOIN `TwoColumnModel` ON `TwoColumnModel`.`id`=`name`",
                join.query.trim()
            )
        }
    }

    @Test
    fun testInnerJoinOnUsing() {
        database<TestDatabase> { db ->
            val join = select from db.simpleModelAdapter innerJoin
                db.twoColumnModelAdapter using SimpleModel_Table.name.withTable()
            assertEquals(
                "SELECT * FROM `SimpleModel` INNER JOIN `TwoColumnModel` USING (`SimpleModel`.`name`)",
                join.query.trim()
            )
        }
    }

    @Test
    fun testNaturalJoin() {
        database<TestDatabase> { db ->
            val join = (select from db.simpleModelAdapter naturalJoin
                db.twoColumnModelAdapter).end()
            assertEquals(
                "SELECT * FROM `SimpleModel` NATURAL JOIN `TwoColumnModel`",
                join.query.trim()
            )
        }
    }
}