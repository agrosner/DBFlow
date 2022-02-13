package com.dbflow5.query2

import com.dbflow5.query2.operations.OperatorGrouping
import com.dbflow5.sql.Query

/**
 * Description:
 */
interface HasOperatorGroup {

    val operatorGroup: OperatorGrouping<Query>
}