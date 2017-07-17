package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.kotlinextensions.crossJoin
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.innerJoin
import com.raizlabs.android.dbflow.kotlinextensions.leftOuterJoin
import com.raizlabs.android.dbflow.kotlinextensions.naturalJoin
import com.raizlabs.android.dbflow.kotlinextensions.on
import com.raizlabs.android.dbflow.kotlinextensions.select
import com.raizlabs.android.dbflow.kotlinextensions.using
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.models.SimpleModel_Table
import com.raizlabs.android.dbflow.models.TwoColumnModel
import com.raizlabs.android.dbflow.models.TwoColumnModel_Table
import org.junit.Assert.assertEquals
import org.junit.Test


class JoinTest : BaseUnitTest() {

    @Test
    fun validateAliasJoin() {
        assertEquals("SELECT * FROM `SimpleModel` INNER JOIN `TwoColumnModel` AS `Name` ON `TwoColumnModel`.`name`=`name`",
            ((select from SimpleModel::class innerJoin
                TwoColumnModel::class).`as`("Name") on TwoColumnModel_Table.name.withTable().eq(SimpleModel_Table.name)).query.trim())
    }

    @Test
    fun testInnerJoin() {
        val join = select from SimpleModel::class innerJoin
            TwoColumnModel::class on TwoColumnModel_Table.name.withTable().eq(SimpleModel_Table.name)
        assertEquals("SELECT * FROM `SimpleModel` INNER JOIN `TwoColumnModel` ON `TwoColumnModel`.`name`=`name`",
            join.query.trim())
    }

    @Test
    fun testLeftOuterJoin() {
        val join = select from SimpleModel::class leftOuterJoin
            TwoColumnModel::class on TwoColumnModel_Table.name.withTable().eq(SimpleModel_Table.name)
        assertEquals("SELECT * FROM `SimpleModel` LEFT OUTER JOIN `TwoColumnModel` ON `TwoColumnModel`.`name`=`name`",
            join.query.trim())
    }

    @Test
    fun testCrossJoin() {
        val join = select from SimpleModel::class crossJoin
            TwoColumnModel::class on TwoColumnModel_Table.name.withTable().eq(SimpleModel_Table.name)
        assertEquals("SELECT * FROM `SimpleModel` CROSS JOIN `TwoColumnModel` ON `TwoColumnModel`.`name`=`name`",
            join.query.trim())
    }

    @Test
    fun testMultiJoin() {
        val join = select from SimpleModel::class innerJoin
            TwoColumnModel::class on TwoColumnModel_Table.name.withTable().eq(SimpleModel_Table.name) crossJoin
            TwoColumnModel::class on TwoColumnModel_Table.id.withTable().eq(SimpleModel_Table.name)
        assertEquals("SELECT * FROM `SimpleModel` INNER JOIN `TwoColumnModel` ON `TwoColumnModel`.`name`=`name`" +
            " CROSS JOIN `TwoColumnModel` ON `TwoColumnModel`.`id`=`name`",
            join.query.trim())
    }

    @Test
    fun testInnerJoinOnUsing() {
        val join = select from SimpleModel::class innerJoin
            TwoColumnModel::class using SimpleModel_Table.name.withTable()
        assertEquals("SELECT * FROM `SimpleModel` INNER JOIN `TwoColumnModel` USING (`SimpleModel`.`name`)",
            join.query.trim())
    }

    @Test
    fun testNaturalJoin() {
        val join = (select from SimpleModel::class naturalJoin
            TwoColumnModel::class).end()
        assertEquals("SELECT * FROM `SimpleModel` NATURAL JOIN `TwoColumnModel`",
            join.query.trim())
    }
}