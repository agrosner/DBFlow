package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.assertEquals
import com.dbflow5.models.TwoColumnModel_Table.id
import com.dbflow5.models.TwoColumnModel_Table.name
import com.dbflow5.query.OperatorGroup
import com.dbflow5.query.and
import com.dbflow5.query.andAll
import com.dbflow5.query.or
import com.dbflow5.query.orAll
import org.junit.Test

class OperatorGroupTest : BaseUnitTest() {


    @Test
    fun validateCommaSeparated() {
        "(`name`='name', `id`=0)".assertEquals(OperatorGroup.clause().setAllCommaSeparated(true).andAll(name.eq("name"), id.eq(0)))
    }

    @Test
    fun validateParanthesis() {
        "`name`='name'".assertEquals(OperatorGroup.nonGroupingClause(name.eq("name")).setUseParenthesis(false))
    }

    @Test
    fun validateOr() {
        "(`name`='name' OR `id`=0)".assertEquals(name.eq("name") or id.eq(0))
    }

    @Test
    fun validateOrAll() {
        "(`name`='name' OR `id`=0 OR `name`='test')".assertEquals(name.eq("name") orAll arrayListOf(id.eq(0), name.eq("test")))
    }

    @Test

    fun validateAnd() {
        "(`name`='name' AND `id`=0)".assertEquals(name.eq("name") and id.eq(0))
    }

    @Test
    fun validateAndAll() {
        "(`name`='name' AND `id`=0 AND `name`='test')".assertEquals(name.eq("name") andAll arrayListOf(id.eq(0), name.eq("test")))
    }

}