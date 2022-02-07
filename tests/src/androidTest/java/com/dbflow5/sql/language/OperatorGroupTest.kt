package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.assertEquals
import com.dbflow5.models.TwoColumnModel_Table
import com.dbflow5.query.OperatorGroup
import com.dbflow5.query.and
import com.dbflow5.query.andAll
import com.dbflow5.query.or
import com.dbflow5.query.orAll
import org.junit.Test

class OperatorGroupTest : BaseUnitTest() {

    @Test
    fun validateCommaSeparated() {
        "(`name`='name', `id`=0)".assertEquals(
            OperatorGroup.clause().setAllCommaSeparated(true).andAll(
                TwoColumnModel_Table.name.eq("name"),
                TwoColumnModel_Table.id.eq(0)
            )
        )
    }

    @Test
    fun validateParanthesis() {
        "`name`='name'".assertEquals(
            OperatorGroup.nonGroupingClause(TwoColumnModel_Table.name.eq("name"))
                .setUseParenthesis(false)
        )
    }

    @Test
    fun validateOr() {
        "(`name`='name' OR `id`=0)".assertEquals(
            TwoColumnModel_Table.name.eq("name") or TwoColumnModel_Table.id.eq(
                0
            )
        )
    }

    @Test
    fun validateOrAll() {
        "(`name`='name' OR `id`=0 OR `name`='test')".assertEquals(
            TwoColumnModel_Table.name.eq("name") orAll arrayListOf(
                TwoColumnModel_Table.id.eq(0),
                TwoColumnModel_Table.name.eq("test")
            )
        )
    }

    @Test

    fun validateAnd() {
        "(`name`='name' AND `id`=0)".assertEquals(
            TwoColumnModel_Table.name.eq("name") and TwoColumnModel_Table.id.eq(
                0
            )
        )
    }

    @Test
    fun validateAndAll() {
        "(`name`='name' AND `id`=0 AND `name`='test')".assertEquals(
            TwoColumnModel_Table.name.eq("name") andAll arrayListOf(
                TwoColumnModel_Table.id.eq(0),
                TwoColumnModel_Table.name.eq("test")
            )
        )
    }

}