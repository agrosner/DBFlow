package com.dbflow5.adapter

/**
 * Description: Provides a set of methods for creating a DB object.
 */
interface CreationAdapter {
    /**
     * @return The query used to create this table.
     */
    val creationQuery: String

    /**
     * @return When false, this table gets generated and associated with database, however it will not immediately
     * get created upon startup. This is useful for keeping around legacy tables for migrations.
     */
    fun createWithDatabase(): Boolean = true
}