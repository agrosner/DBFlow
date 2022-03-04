package com.dbflow5.test.sql.language

import com.dbflow5.assertEquals
import com.dbflow5.test.TwoColumnModel_Table
import com.dbflow5.query.operations.Operation
import com.dbflow5.query.operations.OperatorGroup
import kotlin.test.Test

class OperatorGroupTest {

    @Test
    fun validateCommaSeparated() {
        "(`name` = 'name', `id` = 0)".assertEquals(
            OperatorGroup.clause().chain(
                Operation.Comma,
                TwoColumnModel_Table.name.eq("name"),
                TwoColumnModel_Table.id.eq(0)
            )
        )
    }

    @Test
    fun validateParenthesis() {
        "`name` = 'name'".assertEquals(
            OperatorGroup.nonGroupingClause().chain(
                Operation.Empty,
                TwoColumnModel_Table.name.eq("name")
            )
        )
    }

    @Test
    fun validateOr() {
        "(`name` = 'name' OR `id` = 0)".assertEquals(
            TwoColumnModel_Table.name.eq("name") or TwoColumnModel_Table.id.eq(
                0
            )
        )
    }

    @Test
    fun validateOrAll() {
        "(`name` = 'name' OR `id` = 0 OR `name` = 'test')".assertEquals(
            TwoColumnModel_Table.name.eq("name").chain(
                Operation.Or, listOf(
                    TwoColumnModel_Table.id.eq(0),
                    TwoColumnModel_Table.name.eq("test")
                )
            )
        )
    }

    @Test
    fun validateAnd() {
        "(`name` = 'name' AND `id` = 0)".assertEquals(
            TwoColumnModel_Table.name.eq("name") and TwoColumnModel_Table.id.eq(
                0
            )
        )
    }

    @Test
    fun validateAndAll() {
        "(`name` = 'name' AND `id` = 0 AND `name` = 'test')".assertEquals(
            TwoColumnModel_Table.name.eq("name").chain(
                Operation.And, listOf(
                    TwoColumnModel_Table.id.eq(0),
                    TwoColumnModel_Table.name.eq("test")
                )
            )
        )
    }
}
