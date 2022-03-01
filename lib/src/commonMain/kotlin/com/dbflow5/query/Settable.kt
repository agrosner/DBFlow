package com.dbflow5.query

import com.dbflow5.query.operations.AnyOperator

interface Settable<Table : Any> {
    fun set(vararg conditions: AnyOperator): UpdateWithSet<Table>
    infix fun set(condition: AnyOperator): UpdateWithSet<Table>
}