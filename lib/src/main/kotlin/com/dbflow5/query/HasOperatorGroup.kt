package com.dbflow5.query

import com.dbflow5.query.operations.OperatorGrouping
import com.dbflow5.sql.Query

/**
 * Description:
 */
interface HasOperatorGroup {

    val operatorGroup: OperatorGrouping<Query>
}