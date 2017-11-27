package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.config.databaseForTable
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.models.SimpleModel_Table
import com.raizlabs.android.dbflow.models.TwoColumnModel
import com.raizlabs.android.dbflow.models.TwoColumnModel_Table
import com.raizlabs.android.dbflow.query.crossJoin
import com.raizlabs.android.dbflow.query.innerJoin
import com.raizlabs.android.dbflow.query.leftOuterJoin
import com.raizlabs.android.dbflow.query.naturalJoin
import com.raizlabs.android.dbflow.query.on
import com.raizlabs.android.dbflow.query.select
import com.raizlabs.android.dbflow.query.using
import org.junit.Assert.assertEquals
import org.junit.Test


class JoinTest : BaseUnitTest() {

    @Test
    fun validateAliasJoin() {
        databaseForTable<SimpleModel> {
            assertEquals("SELECT * FROM `SimpleModel` INNER JOIN `TwoColumnModel` AS `Name` ON `TwoColumnModel`.`name`=`name`",
                    ((select from SimpleModel::class innerJoin
                            TwoColumnModel::class).`as`("Name") on TwoColumnModel_Table.name.withTable().eq(SimpleModel_Table.name)).query.trim())
        }
    }

    @Test
    fun testInnerJoin() {
        databaseForTable<SimpleModel> {
            val join = select from SimpleModel::class innerJoin
                    TwoColumnModel::class on TwoColumnModel_Table.name.withTable().eq(SimpleModel_Table.name)
            assertEquals("SELECT * FROM `SimpleModel` INNER JOIN `TwoColumnModel` ON `TwoColumnModel`.`name`=`name`",
                    join.query.trim())
        }
    }

    @Test
    fun testLeftOuterJoin() {
        databaseForTable<SimpleModel> {
            val join = select from SimpleModel::class leftOuterJoin
                    TwoColumnModel::class on TwoColumnModel_Table.name.withTable().eq(SimpleModel_Table.name)
            assertEquals("SELECT * FROM `SimpleModel` LEFT OUTER JOIN `TwoColumnModel` ON `TwoColumnModel`.`name`=`name`",
                    join.query.trim())
        }
    }

    @Test
    fun testCrossJoin() {
        databaseForTable<SimpleModel> {
            val join = select from SimpleModel::class crossJoin
                    TwoColumnModel::class on TwoColumnModel_Table.name.withTable().eq(SimpleModel_Table.name)
            assertEquals("SELECT * FROM `SimpleModel` CROSS JOIN `TwoColumnModel` ON `TwoColumnModel`.`name`=`name`",
                    join.query.trim())
        }
    }

    @Test
    fun testMultiJoin() {
        databaseForTable<SimpleModel> {
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
        databaseForTable<SimpleModel> {
            val join = select from SimpleModel::class innerJoin
                    TwoColumnModel::class using SimpleModel_Table.name.withTable()
            assertEquals("SELECT * FROM `SimpleModel` INNER JOIN `TwoColumnModel` USING (`SimpleModel`.`name`)",
                    join.query.trim())
        }
    }

    @Test
    fun testNaturalJoin() {
        databaseForTable<SimpleModel> {
            val join = (select from SimpleModel::class naturalJoin
                    TwoColumnModel::class).end()
            assertEquals("SELECT * FROM `SimpleModel` NATURAL JOIN `TwoColumnModel`",
                    join.query.trim())
        }
    }
}