package com.dbflow5.test.sql.language

import com.dbflow5.test.assertEquals
import com.dbflow5.test.TwoColumnModel
import com.dbflow5.query.operations.Operation
import com.dbflow5.query.operations.OperatorGroup
import kotlin.test.Test

class OperatorGroupTest {

    @Test
    fun validateCommaSeparated() {
        "(`name` = 'name', `id` = 0)".assertEquals(
            OperatorGroup.clause().chain(
                Operation.Comma,
                TwoColumnModel.name.eq("name"),
                TwoColumnModel.id.eq(0)
            )
        )
    }

    @Test
    fun validateParenthesis() {
        "`name` = 'name'".assertEquals(
            OperatorGroup.nonGroupingClause().chain(
                Operation.Empty,
                TwoColumnModel.name.eq("name")
            )
        )
    }

    @Test
    fun validateOr() {
        "(`name` = 'name' OR `id` = 0)".assertEquals(
            TwoColumnModel.name.eq("name") or TwoColumnModel.id.eq(
                0
            )
        )
    }

    @Test
    fun validateOrAll() {
        "(`name` = 'name' OR `id` = 0 OR `name` = 'test')".assertEquals(
            TwoColumnModel.name.eq("name").chain(
                Operation.Or, listOf(
                    TwoColumnModel.id.eq(0),
                    TwoColumnModel.name.eq("test")
                )
            )
        )
    }

    @Test
    fun validateAnd() {
        "(`name` = 'name' AND `id` = 0)".assertEquals(
            TwoColumnModel.name.eq("name") and TwoColumnModel.id.eq(
                0
            )
        )
    }

    @Test
    fun validateAndAll() {
        "(`name` = 'name' AND `id` = 0 AND `name` = 'test')".assertEquals(
            TwoColumnModel.name.eq("name").chain(
                Operation.And, listOf(
                    TwoColumnModel.id.eq(0),
                    TwoColumnModel.name.eq("test")
                )
            )
        )
    }
}
