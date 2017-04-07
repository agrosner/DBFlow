package com.raizlabs.android.dbflow.sql.language.property

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.sql.language.NameAlias
import org.junit.Assert.assertEquals
import org.junit.Test

class FloatPropertyTest : BaseUnitTest() {

    @Test
    fun testOperators() {
        val prop = Property<Float>(SimpleModel::class.java, "Prop")
        assertEquals("`Prop`=5.0", prop.`is`(5f).query.trim())
        assertEquals("`Prop`=5.0", prop.eq(5f).query.trim())
        assertEquals("`Prop`!=5.0", prop.notEq(5f).query.trim())
        assertEquals("`Prop`!=5.0", prop.isNot(5f).query.trim())
        assertEquals("`Prop`>5.0", prop.greaterThan(5f).query.trim())
        assertEquals("`Prop`>=5.0", prop.greaterThanOrEq(5f).query.trim())
        assertEquals("`Prop`<5.0", prop.lessThan(5f).query.trim())
        assertEquals("`Prop`<=5.0", prop.lessThanOrEq(5f).query.trim())
        assertEquals("`Prop` BETWEEN 5.0 AND 6.0", prop.between(5f).and(6f).query.trim())
        assertEquals("`Prop` IN (5.0,6.0,7.0,8.0)", prop.`in`(5f, 6f, 7f, 8f).query.trim())
        assertEquals("`Prop` NOT IN (5.0,6.0,7.0,8.0)", prop.notIn(5f, 6f, 7f, 8f).query.trim())
        assertEquals("`Prop`=`Prop` + 5.0", prop.concatenate(5f).query.trim())
    }

    @Test
    fun testAlias() {
        val prop = Property<Float>(SimpleModel::class.java, "Prop", "Alias")
        assertEquals("`Prop` AS `Alias`", prop.toString().trim())

        val prop2 = Property<Float>(SimpleModel::class.java,
            NameAlias.builder("Prop")
                .shouldAddIdentifierToName(false)
                .`as`("Alias")
                .shouldAddIdentifierToAliasName(false)
                .build())
        assertEquals("Prop AS Alias", prop2.toString().trim())
    }
}