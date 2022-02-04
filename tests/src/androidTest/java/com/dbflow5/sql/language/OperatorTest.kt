package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.annotation.Collate
import com.dbflow5.assertEquals
import com.dbflow5.config.database
import com.dbflow5.models.TwoColumnModel_Table
import com.dbflow5.query.op
import com.dbflow5.query.select
import org.junit.Test

class OperatorTest : BaseUnitTest() {

    @Test
    fun testEquals() {
        "`name`='name'".assertEquals("name".op<String>().eq("name"))
        "`name`='name'".assertEquals("name".op<String>().`is`("name"))
    }

    @Test
    fun testNotEquals() {
        "`name`!='name'".assertEquals("name".op<String>().notEq("name"))
        "`name`!='name'".assertEquals("name".op<String>().isNot("name"))
    }

    @Test
    fun testLike() {
        "`name` LIKE 'name'".assertEquals("name".op<String>().like("name"))
        "`name` NOT LIKE 'name'".assertEquals("name".op<String>().notLike("name"))
        "`name` GLOB 'name'".assertEquals("name".op<String>().glob("name"))
    }

    @Test
    fun testMath() {
        "`name`>'name'".assertEquals("name".op<String>().greaterThan("name"))
        "`name`>='name'".assertEquals("name".op<String>().greaterThanOrEq("name"))
        "`name`<'name'".assertEquals("name".op<String>().lessThan("name"))
        "`name`<='name'".assertEquals("name".op<String>().lessThanOrEq("name"))
        "`name`+'name'".assertEquals("name".op<String>() + "name")
        "`name`-'name'".assertEquals("name".op<String>() - "name")
        "`name`/'name'".assertEquals("name".op<String>() / "name")
        "`name`*'name'".assertEquals("name".op<String>() * "name")
        "`name`%'name'".assertEquals("name".op<String>() % "name")
    }

    @Test
    fun testCollate() {
        "`name` COLLATE NOCASE".assertEquals("name".op<String>() collate Collate.NOCASE)
        "`name` COLLATE NOCASE".assertEquals("name".op<String>() collate "NOCASE")
    }

    @Test
    fun testBetween() {
        "`id` BETWEEN 6 AND 7".assertEquals(TwoColumnModel_Table.id.between(6) and 7)
    }

    @Test
    fun testIn() {
        database<TestDatabase> { db ->
            "`id` IN (5,6,7,8,9)".assertEquals(TwoColumnModel_Table.id.`in`(5, 6, 7, 8) and 9)
            "`id` NOT IN (SELECT * FROM `SimpleModel`)".assertEquals(
                TwoColumnModel_Table.id.notIn(
                    select from db.simpleModelAdapter
                )
            )
        }
    }

    @Test
    fun matchOperator() {
        "`name` MATCH 'age'".assertEquals("name".op<String>() match "age")
    }

}