package com.dbflow5.query2

import com.dbflow5.query.SQLOperator

/**
 * Description:
 */
interface HavingEnabled<Table : Any, OperationBase> {

    infix fun having(operator: SQLOperator): WhereWithHaving<Table, OperationBase>
    fun having(vararg operators: SQLOperator): WhereWithHaving<Table, OperationBase>
}