package com.dbflow5.sql.language.property

import com.dbflow5.BaseUnitTest
import com.dbflow5.models.SimpleModel
import com.dbflow5.query.NameAlias
import com.dbflow5.query.property.Property
import org.junit.Assert.assertEquals
import org.junit.Test

class DoublePropertyTest : BaseUnitTest() {

    @Test
    fun testOperators() {
        val prop = Property<Double>(SimpleModel::class.java, "Prop")
        assertEquals("`Prop`=5.0", prop.`is`(5.0).query.trim())
        assertEquals("`Prop`=5.0", prop.eq(5.0).query.trim())
        assertEquals("`Prop`!=5.0", prop.notEq(5.0).query.trim())
        assertEquals("`Prop`!=5.0", prop.isNot(5.0).query.trim())
        assertEquals("`Prop`>5.0", prop.greaterThan(5.0).query.trim())
        assertEquals("`Prop`>=5.0", prop.greaterThanOrEq(5.0).query.trim())
        assertEquals("`Prop`<5.0", prop.lessThan(5.0).query.trim())
        assertEquals("`Prop`<=5.0", prop.lessThanOrEq(5.0).query.trim())
        assertEquals("`Prop` BETWEEN 5.0 AND 6.0", prop.between(5.0).and(6.0).query.trim())
        assertEquals("`Prop` IN (5.0,6.0,7.0,8.0)", prop.`in`(5.0, 6.0, 7.0, 8.0).query.trim())
        assertEquals("`Prop` NOT IN (5.0,6.0,7.0,8.0)", prop.notIn(5.0, 6.0, 7.0, 8.0).query.trim())
        assertEquals("`Prop`=`Prop` + 5.0", prop.concatenate(5.0).query.trim())
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