package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.assertEquals
import com.raizlabs.android.dbflow.kotlinextensions.and
import com.raizlabs.android.dbflow.kotlinextensions.andAll
import com.raizlabs.android.dbflow.kotlinextensions.or
import com.raizlabs.android.dbflow.kotlinextensions.orAll
import com.raizlabs.android.dbflow.models.TwoColumnModel_Table.id
import com.raizlabs.android.dbflow.models.TwoColumnModel_Table.name
import org.junit.Test

class OperatorGroupTest : BaseUnitTest() {


    @Test
    fun validateCommaSeparated() {
        assertEquals("(`name`='name', `id`=0)", OperatorGroup.clause().setAllCommaSeparated(true).andAll(name.eq("name"), id.eq(0)))
    }

    @Test
    fun validateParanthesis() {
        assertEquals("`name`='name'", OperatorGroup.nonGroupingClause(name.eq("name")).setUseParenthesis(false))
    }

    @Test
    fun validateOr() {
        assertEquals("(`name`='name' OR `id`=0)", name.eq("name") or id.eq(0))
    }

    @Test
    fun validateOrAll() {
        assertEquals("(`name`='name' OR `id`=0 OR `name`='test')", name.eq("name") orAll arrayListOf(id.eq(0), name.eq("test")))
    }

    @Test
    fun validateAnd() {
        assertEquals("(`name`='name' AND `id`=0)", name.eq("name") and id.eq(0))
    }

    @Test
    fun validateAndAll() {
        assertEquals("(`name`='name' AND `id`=0 AND `name`='test')", name.eq("name") andAll arrayListOf(id.eq(0), name.eq("test")))
    }

}