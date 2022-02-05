package com.dbflow5.query2

import com.dbflow5.query.SQLOperator

/**
 * Description:
 */
interface Whereable<Table : Any, OperationBase> {

    infix fun where(operator: SQLOperator): Where<Table, OperationBase>
}