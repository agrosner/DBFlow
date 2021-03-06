package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.query.NameAlias
import com.dbflow5.query.`as`
import com.dbflow5.query.nameAlias
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class NameAliasTest : BaseUnitTest() {

    @Test
    fun testSimpleCase() {
        assertEquals("`name`", "name".nameAlias.query)
    }

    @Test
    fun testAlias() {
        assertEquals("`name` AS `alias`", "name".`as`("alias").fullQuery)
    }

    @Test
    fun validateBuilder() {
        val nameAlias = NameAlias.builder("name")
                .keyword("DISTINCT")
                .`as`("Alias")
                .withTable("MyTable")
                .shouldAddIdentifierToAliasName(false)
                .shouldAddIdentifierToName(false)
                .shouldStripAliasName(false)
                .shouldStripIdentifier(false).build()
        assertEquals("DISTINCT", nameAlias.keyword)
        assertEquals("Alias", nameAlias.aliasName())
        assertEquals("Alias", nameAlias.aliasNameRaw())
        assertEquals("`MyTable`", nameAlias.tableName)
        assertFalse(nameAlias.shouldStripAliasName)
        assertFalse(nameAlias.shouldStripIdentifier)
        assertEquals("Alias", nameAlias.nameAsKey)
        assertEquals("DISTINCT `MyTable`.name AS Alias", nameAlias.fullQuery)
    }
}