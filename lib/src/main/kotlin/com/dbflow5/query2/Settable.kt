package com.dbflow5.query2

import com.dbflow5.query2.operations.AnyOperator

interface Settable<Table : Any> {
    fun set(vararg conditions: AnyOperator): UpdateWithSet<Table>
    infix fun set(condition: AnyOperator): UpdateWithSet<Table>
}