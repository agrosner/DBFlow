package com.dbflow5.query2

interface Limitable<Table : Any, OperationBase> {

    infix fun limit(count: Long): WhereWithLimit<Table, OperationBase>
}