package com.dbflow5.query2

interface WhereExistsEnabled<Table : Any, OperationBase> {

    infix fun <OtherTable : Any, OtherOperationBase> whereExists(
        whereable: Where<OtherTable, OtherOperationBase>
    ): WhereExists<Table, OperationBase>

    fun <OtherTable : Any, OtherOperationBase> whereExists(
        not: Boolean = false,
        whereable: Where<OtherTable, OtherOperationBase>
    ): WhereExists<Table, OperationBase>
}
