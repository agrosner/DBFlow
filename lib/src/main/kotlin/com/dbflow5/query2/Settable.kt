package com.dbflow5.query2

import com.dbflow5.query.SQLOperator

interface Settable<Table : Any> {
    fun set(vararg conditions: SQLOperator): UpdateWithSet<Table>
    infix fun set(condition: SQLOperator): UpdateWithSet<Table>
}