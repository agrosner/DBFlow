package com.dbflow5.sql.language.property

import com.dbflow5.TestDatabase_Database
import com.dbflow5.assertEquals
import com.dbflow5.models.SimpleModel
import com.dbflow5.query.NameAlias
import com.dbflow5.query.nameAlias
import com.dbflow5.query.operations.PropertyStart
import com.dbflow5.query.operations.concatenate
import com.dbflow5.query.operations.glob
import com.dbflow5.query.operations.like
import com.dbflow5.query.operations.match
import com.dbflow5.query.operations.notLike
import com.dbflow5.query.operations.property
import com.dbflow5.simpleModelAdapter
import com.dbflow5.test.DatabaseTestRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PropertyTest {

    
    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun testOperators() = dbRule {
        val prop: PropertyStart<String, SimpleModel> = simpleModelAdapter.property("Prop".nameAlias)
        "`Prop` = '5'".assertEquals(prop eq "5")
        "`Prop` != '5'".assertEquals(prop notEq "5")
        "`Prop` LIKE '5'".assertEquals(prop like "5")
        "`Prop` NOT LIKE '5'".assertEquals(prop notLike "5")
        "`Prop` GLOB '5'".assertEquals(prop glob "5")
        "`Prop` > '5'".assertEquals(prop greaterThan "5")
        "`Prop` >= '5'".assertEquals(prop greaterThanOrEq "5")
        "`Prop` < '5'".assertEquals(prop lessThan "5")
        "`Prop` <= '5'".assertEquals(prop lessThanOrEq "5")
        "`Prop` BETWEEN '5' AND '6'".assertEquals((prop between "5") and "6")
        "`Prop` IN('5','6','7','8')".assertEquals(prop.`in`("5", "6", "7", "8"))
        "`Prop` NOT IN('5','6','7','8')".assertEquals(prop.notIn("5", "6", "7", "8"))
        "`Prop` || '5'".assertEquals(prop concatenate "5")
        "`Prop` MATCH 'age'".assertEquals(prop match "age")
    }

    @Test
    fun testAlias() = dbRule {
        val prop = simpleModelAdapter.property<String, SimpleModel>("Prop".nameAlias).`as`("Alias")
        assertEquals("`Prop` AS `Alias`", prop.query)

        val prop2 = simpleModelAdapter.property<String, SimpleModel>(
            NameAlias.builder("Prop")
                .shouldAddIdentifierToName(false)
                .`as`("Alias")
                .shouldAddIdentifierToAliasName(false)
                .build()
        )
        assertEquals("Prop AS Alias", prop2.query)
    }
}