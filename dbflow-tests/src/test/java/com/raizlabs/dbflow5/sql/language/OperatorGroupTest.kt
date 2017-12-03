package com.raizlabs.dbflow5.sql.language

import com.raizlabs.dbflow5.BaseUnitTest
import com.raizlabs.dbflow5.assertEquals
import com.raizlabs.dbflow5.models.TwoColumnModel_Table.id
import com.raizlabs.dbflow5.models.TwoColumnModel_Table.name
import com.raizlabs.dbflow5.query.OperatorGroup
import com.raizlabs.dbflow5.query.and
import com.raizlabs.dbflow5.query.andAll
import com.raizlabs.dbflow5.query.or
import com.raizlabs.dbflow5.query.orAll
import org.junit.Test

class OperatorGroupTest : BaseUnitTest() {


    @Test
    fun validateCommaSeparated() {
        assertEquals("(`name`='name', `id`=0)",
                OperatorGroup.clause().setAllCommaSeparated(true).andAll(name.eq("name"), id.eq(0)))
    }

    @Test
    fun validateParanthesis() {
        assertEquals("`name`='name'",
                OperatorGroup.nonGroupingClause(name.eq("name")).setUseParenthesis(false))
    }

    @Test
    fun validateOr() {
        assertEquals("(`name`='name' OR `id`=0)",
                name.eq("name") or id.eq(0))
    }

    @Test
    fun validateOrAll() {
        assertEquals("(`name`='name' OR `id`=0 OR `name`='test')",
                name.eq("name") orAll arrayListOf(id.eq(0), name.eq("test")))
    }

    @Test

    fun validateAnd() {
        assertEquals("(`name`='name' AND `id`=0)", name.eq("name") and id.eq(0))
    }

    @Test
    fun validateAndAll() {
        assertEquals("(`name`='name' AND `id`=0 AND `name`='test')",
                name.eq("name") andAll arrayListOf(id.eq(0), name.eq("test")))
    }

}