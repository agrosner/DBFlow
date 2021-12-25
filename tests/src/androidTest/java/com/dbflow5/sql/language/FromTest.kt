package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.models.TwoColumnModel
import com.dbflow5.models.TwoColumnModel_Table
import com.dbflow5.query.select
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FromTest : BaseUnitTest() {

    @Test
    fun validateSimpleFrom() {
        assertEquals("SELECT * FROM `SimpleModel`", (select from SimpleModel::class).query.trim())
    }

    @Test
    fun validateProjectionFrom() {
        assertEquals(
            "SELECT `name` FROM `SimpleModel`",
            (select(SimpleModel_Table.name) from SimpleModel::class).query.trim()
        )
    }

    @Test
    fun validateMultipleProjection() {
        assertEquals(
            "SELECT `name`,`name`,`id` FROM `SimpleModel`",
            (select(
                SimpleModel_Table.name,
                TwoColumnModel_Table.name,
                TwoColumnModel_Table.id
            ) from SimpleModel::class).query.trim()
        )
    }

    @Test
    fun validateAlias() {
        assertEquals(
            "SELECT * FROM `SimpleModel` AS `Simple`",
            (select from SimpleModel::class `as` "Simple").query.trim()
        )
    }

    @Test
    fun validateJoins() {
        val from = (select from SimpleModel::class
            innerJoin TwoColumnModel::class
            on SimpleModel_Table.name.eq(TwoColumnModel_Table.name.withTable()))
        assertEquals(
            "SELECT * FROM `SimpleModel` INNER JOIN `TwoColumnModel` ON `name`=`TwoColumnModel`.`name`",
            from.query.trim()
        )
        assertTrue(from.associatedTables.isNotEmpty())
    }
}