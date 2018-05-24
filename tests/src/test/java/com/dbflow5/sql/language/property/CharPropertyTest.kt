package com.dbflow5.sql.language.property

import com.dbflow5.BaseUnitTest
import com.dbflow5.models.SimpleModel
import com.dbflow5.query.NameAlias
import com.dbflow5.query.property.Property
import org.junit.Assert.assertEquals
import org.junit.Test

class CharPropertyTest : BaseUnitTest() {

    @Test
    fun testOperators() {
        val prop = Property<Char>(SimpleModel::class.java, "Prop")
        assertEquals("`Prop`='5'", prop.`is`('5').query.trim())
        assertEquals("`Prop`='5'", prop.eq('5').query.trim())
        assertEquals("`Prop`!='5'", prop.notEq('5').query.trim())
        assertEquals("`Prop`!='5'", prop.isNot('5').query.trim())
        assertEquals("`Prop`>'5'", prop.greaterThan('5').query.trim())
        assertEquals("`Prop`>='5'", prop.greaterThanOrEq('5').query.trim())
        assertEquals("`Prop`<'5'", prop.lessThan('5').query.trim())
        assertEquals("`Prop`<='5'", prop.lessThanOrEq('5').query.trim())
        assertEquals("`Prop` BETWEEN '5' AND '6'", prop.between('5').and('6').query.trim())
        assertEquals("`Prop` IN ('5','6','7','8')", prop.`in`('5', '6', '7', '8').query.trim())
        assertEquals("`Prop` NOT IN ('5','6','7','8')", prop.notIn('5', '6', '7', '8').query.trim())
        assertEquals("`Prop`=`Prop` || '5'", prop.concatenate('5').query.trim())
    }

    @Test
    fun testAlias() {
        val prop = Property<Char>(SimpleModel::class.java, "Prop", "Alias")
        assertEquals("`Prop` AS `Alias`", prop.toString().trim())

        val prop2 = Property<Char>(SimpleModel::class.java,
                NameAlias.builder("Prop")
                        .shouldAddIdentifierToName(false)
                        .`as`("Alias")
                        .shouldAddIdentifierToAliasName(false)
                        .build())
        assertEquals("Prop AS Alias", prop2.toString().trim())
    }
}