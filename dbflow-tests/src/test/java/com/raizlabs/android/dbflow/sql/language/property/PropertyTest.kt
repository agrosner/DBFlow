package com.raizlabs.android.dbflow.sql.language.property

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.sql.language.NameAlias
import org.junit.Assert
import org.junit.Test

class PropertyTest : BaseUnitTest() {

    @Test
    fun testOperators() {
        val prop = Property<String>(SimpleModel::class.java, "Prop")
        Assert.assertEquals("`Prop`='5'", prop.`is`("5").query.trim())
        Assert.assertEquals("`Prop`='5'", prop.eq("5").query.trim())
        Assert.assertEquals("`Prop`!='5'", prop.notEq("5").query.trim())
        Assert.assertEquals("`Prop`!='5'", prop.isNot("5").query.trim())
        Assert.assertEquals("`Prop` LIKE '5'", prop.like("5").query.trim())
        Assert.assertEquals("`Prop` NOT LIKE '5'", prop.notLike("5").query.trim())
        Assert.assertEquals("`Prop` GLOB '5'", prop.glob("5").query.trim())
        Assert.assertEquals("`Prop`>'5'", prop.greaterThan("5").query.trim())
        Assert.assertEquals("`Prop`>='5'", prop.greaterThanOrEq("5").query.trim())
        Assert.assertEquals("`Prop`<'5'", prop.lessThan("5").query.trim())
        Assert.assertEquals("`Prop`<='5'", prop.lessThanOrEq("5").query.trim())
        Assert.assertEquals("`Prop` BETWEEN '5' AND '6'", prop.between("5").and("6").query.trim())
        Assert.assertEquals("`Prop` IN ('5','6','7','8')", prop.`in`("5", "6", "7", "8").query.trim())
        Assert.assertEquals("`Prop` NOT IN ('5','6','7','8')", prop.notIn("5", "6", "7", "8").query.trim())
        Assert.assertEquals("`Prop`=`Prop` || '5'", prop.concatenate("5").query.trim())
    }

    @Test
    fun testAlias() {
        val prop = Property<String>(SimpleModel::class.java, "Prop", "Alias")
        Assert.assertEquals("`Prop` AS `Alias`", prop.toString().trim())

        val prop2 = Property<String>(SimpleModel::class.java,
            NameAlias.builder("Prop")
                .shouldAddIdentifierToName(false)
                .`as`("Alias")
                .shouldAddIdentifierToAliasName(false)
                .build())
        Assert.assertEquals("Prop AS Alias", prop2.toString().trim())
    }
}