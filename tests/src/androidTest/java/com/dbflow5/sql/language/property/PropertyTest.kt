package com.dbflow5.sql.language.property

import com.dbflow5.BaseUnitTest
import com.dbflow5.assertEquals
import com.dbflow5.models.SimpleModel
import com.dbflow5.query.NameAlias
import com.dbflow5.query.property.Property
import org.junit.Assert.assertEquals
import org.junit.Test

class PropertyTest : BaseUnitTest() {

    @Test
    fun testOperators() {
        val prop = Property<String>(SimpleModel::class, "Prop")
        "`Prop`='5'".assertEquals(prop `is` "5")
        "`Prop`='5'".assertEquals(prop eq "5")
        "`Prop`!='5'".assertEquals(prop notEq "5")
        "`Prop`!='5'".assertEquals(prop isNot "5")
        "`Prop` LIKE '5'".assertEquals(prop like "5")
        "`Prop` NOT LIKE '5'".assertEquals(prop notLike "5")
        "`Prop` GLOB '5'".assertEquals(prop glob "5")
        "`Prop`>'5'".assertEquals(prop greaterThan "5")
        "`Prop`>='5'".assertEquals(prop greaterThanOrEq "5")
        "`Prop`<'5'".assertEquals(prop lessThan "5")
        "`Prop`<='5'".assertEquals(prop lessThanOrEq "5")
        "`Prop` BETWEEN '5' AND '6'".assertEquals((prop between "5") and "6")
        "`Prop` IN ('5','6','7','8')".assertEquals(prop.`in`("5", "6", "7", "8"))
        "`Prop` NOT IN ('5','6','7','8')".assertEquals(prop.notIn("5", "6", "7", "8"))
        "`Prop`=`Prop` || '5'".assertEquals(prop concatenate "5")
        "`Prop` MATCH 'age'".assertEquals(prop match "age")
    }

    @Test
    fun testAlias() {
        val prop = Property<String>(SimpleModel::class, "Prop", "Alias")
        assertEquals("`Prop` AS `Alias`", prop.toString().trim())

        val prop2 = Property<String>(SimpleModel::class,
                NameAlias.builder("Prop")
                        .shouldAddIdentifierToName(false)
                        .`as`("Alias")
                        .shouldAddIdentifierToAliasName(false)
                        .build())
        assertEquals("Prop AS Alias", prop2.toString().trim())
    }
}