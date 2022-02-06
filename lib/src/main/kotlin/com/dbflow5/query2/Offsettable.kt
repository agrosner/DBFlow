package com.dbflow5.query2

interface Offsettable<Table : Any, OperationBase> {
    infix fun offset(offset: Long): WhereWithOffset<Table, OperationBase>
}