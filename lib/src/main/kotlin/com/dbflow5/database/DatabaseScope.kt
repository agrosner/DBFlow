package com.dbflow5.database

import com.dbflow5.config.DBFlowDatabase

/**
 * Description:
 */
interface DatabaseScope<DB : DBFlowDatabase> {

    val db: DBFlowDatabase
}
