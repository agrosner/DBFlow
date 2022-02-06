package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.models.TwoColumnModel_Table
import com.dbflow5.query2.crossJoin
import com.dbflow5.query2.innerJoin
import com.dbflow5.query2.leftOuterJoin
import com.dbflow5.query2.naturalJoin
import com.dbflow5.query2.select
import org.junit.Test
import kotlin.test.assertEquals


class JoinTest : BaseUnitTest() {

    @Test
    fun validateAliasJoin() {
        database<TestDatabase> { db ->
            assertEquals(
                "SELECT * FROM `SimpleModel` INNER JOIN `TwoColumnModel` AS `Name` ON `TwoColumnModel`.`name`=`name`",
                ((db.simpleModelAdapter.select() innerJoin
                    db.twoColumnModelAdapter).`as`("Name") on TwoColumnModel_Table.name.withTable()
                    .eq(SimpleModel_Table.name)).query.trim()
            )
        }
    }

    @Test
    fun testInnerJoin() {
        database<TestDatabase> { db ->
            val join = db.simpleModelAdapter.select() innerJoin
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
            val join = db.simpleModelAdapter.select() leftOuterJoin
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
            val join = db.simpleModelAdapter.select() crossJoin
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
            val join = db.simpleModelAdapter.select() innerJoin
                db.twoColumnModelAdapter on TwoColumnModel_Table.name.withTable()
                .eq(SimpleModel_Table.name) crossJoin
                db.twoColumnModelAdapter on TwoColumnModel_Table.id.withTable()
                .eq(SimpleModel_Table.name)
            val query = join.query
            assertEquals(
                "SELECT * FROM `SimpleModel`" +
                    " INNER JOIN `TwoColumnModel` ON `TwoColumnModel`.`name`=`name`" +
                    " CROSS JOIN `TwoColumnModel` ON `TwoColumnModel`.`id`=`name`",
                join.query.trim()
            )
        }
    }

    @Test
    fun testInnerJoinOnUsing() {
        database<TestDatabase> { db ->
            val join = db.simpleModelAdapter.select() innerJoin
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
            val join = (db.simpleModelAdapter.select() naturalJoin
                db.twoColumnModelAdapter)
            assertEquals(
                "SELECT * FROM `SimpleModel` NATURAL JOIN `TwoColumnModel`",
                join.query.trim()
            )
        }
    }
}