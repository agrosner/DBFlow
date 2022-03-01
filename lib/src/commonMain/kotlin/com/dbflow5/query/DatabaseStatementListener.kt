package com.dbflow5.query

import com.dbflow5.annotation.Table
import com.dbflow5.database.DatabaseStatement

/**
 * Have your [Table] data class implement this interface to receive event hooks into DatabaseStatement
 * lifecycle.
 */
interface DatabaseStatementListener {

    enum class Type {
        Insert,
        Update,
        Delete
    }

    /**
     * Called exactly after a [DatabaseStatement] is bound with data.
     */
    fun onBind(type: Type, databaseStatement: DatabaseStatement)
}
