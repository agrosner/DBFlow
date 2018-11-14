package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.config.databaseForTable
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.models.TwoColumnModel
import com.dbflow5.models.TwoColumnModel_Table
import com.dbflow5.query.select
import org.junit.Assert.assertEquals
import org.junit.Test


class JoinTest : BaseUnitTest() {

    @Test
    fun validateAliasJoin() {
        databaseForTable<SimpleModel> { dbFlowDatabase ->
            assertEquals("SELECT * FROM `SimpleModel` INNER JOIN `TwoColumnModel` AS `Name` ON `TwoColumnModel`.`name`=`name`",
                    ((select from SimpleModel::class innerJoin
                            TwoColumnModel::class).`as`("Name") on TwoColumnModel_Table.name.withTable().eq(SimpleModel_Table.name)).query.trim())
        }
    }

    @Test
    fun testInnerJoin() {
        databaseForTable<SimpleModel> { dbFlowDatabase ->
            val join = select from SimpleModel::class innerJoin
                    TwoColumnModel::class on TwoColumnModel_Table.name.withTable().eq(SimpleModel_Table.name)
            assertEquals("SELECT * FROM `SimpleModel` INNER JOIN `TwoColumnModel` ON `TwoColumnModel`.`name`=`name`",
                    join.query.trim())
        }
    }

    @Test
    fun testLeftOuterJoin() {
        databaseForTable<SimpleModel> { dbFlowDatabase ->
            val join = select from SimpleModel::class leftOuterJoin
                    TwoColumnModel::class on TwoColumnModel_Table.name.withTable().eq(SimpleModel_Table.name)
            assertEquals("SELECT * FROM `SimpleModel` LEFT OUTER JOIN `TwoColumnModel` ON `TwoColumnModel`.`name`=`name`",
                    join.query.trim())
        }
    }

    @Test
    fun testCrossJoin() {
        databaseForTable<SimpleModel> { dbFlowDatabase ->
            val join = select from SimpleModel::class crossJoin
                    TwoColumnModel::class on TwoColumnModel_Table.name.withTable().eq(SimpleModel_Table.name)
            assertEquals("SELECT * FROM `SimpleModel` CROSS JOIN `TwoColumnModel` ON `TwoColumnModel`.`name`=`name`",
                    join.query.trim())
        }
    }

    @Test
    fun testMultiJoin() {
        databaseForTable<SimpleModel> { dbFlowDatabase ->
            val join = select from SimpleModel::class innerJoin
                    TwoColumnModel::class on TwoColumnModel_Table.name.withTable().eq(SimpleModel_Table.name) crossJoin
                    TwoColumnModel::class on TwoColumnModel_Table.id.withTable().eq(SimpleModel_Table.name)
            assertEquals("SELECT * FROM `SimpleModel` INNER JOIN `TwoColumnModel` ON `TwoColumnModel`.`name`=`name`" +
                    " CROSS JOIN `TwoColumnModel` ON `TwoColumnModel`.`id`=`name`",
                    join.query.trim())
        }
    }

    @Test
    fun testInnerJoinOnUsing() {
        databaseForTable<SimpleModel> { dbFlowDatabase ->
            val join = select from SimpleModel::class innerJoin
                    TwoColumnModel::class using SimpleModel_Table.name.withTable()
            assertEquals("SELECT * FROM `SimpleModel` INNER JOIN `TwoColumnModel` USING (`SimpleModel`.`name`)",
                    join.query.trim())
        }
    }

    @Test
    fun testNaturalJoin() {
        databaseForTable<SimpleModel> { dbFlowDatabase ->
            val join = (select from SimpleModel::class naturalJoin
                    TwoColumnModel::class).end()
            assertEquals("SELECT * FROM `SimpleModel` NATURAL JOIN `TwoColumnModel`",
                    join.query.trim())
        }
    }
}