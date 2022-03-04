package com.dbflow5.sql.language

import com.dbflow5.TestDatabase_Database
import com.dbflow5.annotation.Collate
import com.dbflow5.assertEquals
import com.dbflow5.models.TwoColumnModel_Table
import com.dbflow5.query.nameAlias
import com.dbflow5.query.operations.Operation
import com.dbflow5.query.operations.collate
import com.dbflow5.query.operations.operator
import com.dbflow5.query.select
import com.dbflow5.simpleModelAdapter
import com.dbflow5.test.DatabaseTestRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class OperatorTest {

    
    val dbRule = DatabaseTestRule(TestDatabase_Database)

    private val name = "name".nameAlias

    @Test
    fun testEquals() {
        "`name` = 'name'".assertEquals(name eq "name")
    }

    @Test
    fun testNotEquals() {
        "`name` != 'name'".assertEquals(name notEq "name")
    }

    @Test
    fun testLike() {
        "`name` LIKE 'name'".assertEquals(operator(name, Operation.Like, "name"))
        "`name` NOT LIKE 'name'".assertEquals(operator(name, Operation.NotLike, "name"))
        "`name` GLOB 'name'".assertEquals(operator(name, Operation.Glob, "name"))
    }

    @Test
    fun testMath() {
        "`name` > 'name'".assertEquals(name greaterThan "name")
        "`name` >= 'name'".assertEquals(name greaterThanOrEq "name")
        "`name` < 'name'".assertEquals(name lessThan "name")
        "`name` <= 'name'".assertEquals(name lessThanOrEq "name")
        "`name` + 'name'".assertEquals(name + "name")
        "`name` - 'name'".assertEquals(name - "name")
        "`name` / 'name'".assertEquals(name / "name")
        "`name` * 'name'".assertEquals(name * "name")
        "`name` % 'name'".assertEquals(name % "name")
    }

    @Test
    fun testCollate() {
        "`name` COLLATE NOCASE".assertEquals(operator(name) collate Collate.NoCase)
    }

    @Test
    fun testBetween() {
        "`id` BETWEEN 6 AND 7".assertEquals(TwoColumnModel_Table.id between 6 and 7)
    }

    @Test
    fun testIn() = runTest {
        dbRule {
            "`id` IN(5,6,7,8,9)".assertEquals(TwoColumnModel_Table.id.`in`(5, 6, 7, 8, 9))
            "`id` NOT IN(SELECT * FROM `SimpleModel`)".assertEquals(
                TwoColumnModel_Table.id.notIn(
                    select from simpleModelAdapter
                )
            )
        }
    }

    @Test
    fun matchOperator() {
        "`name` MATCH 'age'".assertEquals(operator(name, Operation.Match, "age"))
    }

}