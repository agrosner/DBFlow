package com.dbflow5.sql.language.property

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.assertEquals
import com.dbflow5.config.database
import com.dbflow5.models.SimpleModel
import com.dbflow5.query.NameAlias
import com.dbflow5.query.nameAlias
import com.dbflow5.query2.operations.PropertyStart
import com.dbflow5.query2.operations.concatenate
import com.dbflow5.query2.operations.glob
import com.dbflow5.query2.operations.like
import com.dbflow5.query2.operations.match
import com.dbflow5.query2.operations.notLike
import com.dbflow5.query2.operations.property
import org.junit.Assert.assertEquals
import org.junit.Test

class PropertyTest : BaseUnitTest() {

    private val simpleModelAdapter
        get() = database<TestDatabase>().simpleModelAdapter

    @Test
    fun testOperators() {
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
    fun testAlias() {
        val prop = simpleModelAdapter.property<String, SimpleModel>("Prop".nameAlias).`as`("Alias")
        assertEquals("`Prop` AS `Alias`", prop.query)

        val prop2 = simpleModelAdapter.property<String, SimpleModel>(
            NameAlias.builder("Prop")
                .shouldAddIdentifierToName(false)
                .`as`("Alias")
                .shouldAddIdentifierToAliasName(false)
                .build()
        )
        assertEquals("Prop AS Alias", prop2.toString().trim())
    }
}