package com.dbflow5.query2

interface Constrainable<Table : Any, OperationBase> {

    fun constrain(offset: Long, limit: Long): WhereWithOffset<Table, OperationBase>
}