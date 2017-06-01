package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.annotation.Collate
import com.raizlabs.android.dbflow.assertEquals
import com.raizlabs.android.dbflow.kotlinextensions.*
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.models.TwoColumnModel_Table.id
import org.junit.Test

class OperatorTest : BaseUnitTest() {

    @Test
    fun testEquals() {
        assertEquals("`name`='name'", "name".op<String>().eq("name"))
        assertEquals("`name`='name'", "name".op<String>().`is`("name"))
    }

    @Test
    fun testNotEquals() {
        assertEquals("`name`!='name'", "name".op<String>().notEq("name"))
        assertEquals("`name`!='name'", "name".op<String>().isNot("name"))
    }

    @Test
    fun testLike() {
        assertEquals("`name` LIKE 'name'", "name".op<String>().like("name"))
        assertEquals("`name` NOT LIKE 'name'", "name".op<String>().notLike("name"))
        assertEquals("`name` GLOB 'name'", "name".op<String>().glob("name"))
    }

    @Test
    fun testMath() {
        assertEquals("`name`>'name'", "name".op<String>().greaterThan("name"))
        assertEquals("`name`>='name'", "name".op<String>().greaterThanOrEq("name"))
        assertEquals("`name`<'name'", "name".op<String>().lessThan("name"))
        assertEquals("`name`<='name'", "name".op<String>().lessThanOrEq("name"))
        assertEquals("`name`+'name'", "name".op<String>() + "name")
        assertEquals("`name`-'name'", "name".op<String>() - "name")
        assertEquals("`name`/'name'", "name".op<String>() / "name")
        assertEquals("`name`*'name'", "name".op<String>() * "name")
        assertEquals("`name`%'name'", "name".op<String>() % "name")
    }

    @Test
    fun testCollate() {
        assertEquals("`name` COLLATE NOCASE", "name".op<String>() collate Collate.NOCASE)
        assertEquals("`name` COLLATE NOCASE", "name".op<String>() collate "NOCASE")
    }

    @Test
    fun testBetween() {
        assertEquals("`id` BETWEEN 6 AND 7", id between 6 and 7)
    }

    @Test
    fun testIn() {
        assertEquals("`id` IN (5,6,7,8,9)", id.`in`(5, 6, 7, 8) and 9)
        assertEquals("`id` NOT IN (SELECT * FROM `SimpleModel`)", id.notIn(select from SimpleModel::class))
    }
}