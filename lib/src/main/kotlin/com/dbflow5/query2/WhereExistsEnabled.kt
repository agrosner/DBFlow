package com.dbflow5.query2

interface WhereExistsEnabled<
    Table : Any,
    Result,
    OperationBase : ExecutableQuery<Result>> {

    infix fun <OtherTable : Any, OtherOperationBase : ExecutableQuery<Result>> whereExists(
        whereable: Where<OtherTable, Result, OtherOperationBase>
    ): WhereExists<Table, Result, OperationBase>

    fun <OtherTable : Any, OtherOperationBase : ExecutableQuery<Result>> whereExists(
        not: Boolean = false,
        whereable: Where<OtherTable, Result, OtherOperationBase>
    ): WhereExists<Table,Result,  OperationBase>
}
