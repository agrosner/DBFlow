package com.dbflow5.database.scope

import com.dbflow5.database.GeneratedDatabase

/**
 * Base scope that all transactions and db access contain.
 */
interface DatabaseScope<DB : GeneratedDatabase> {
    val db: DB
}
