package com.dbflow5.adapter

import com.dbflow5.database.DatabaseWrapper

enum class ObjectType(val value: String) {
    View("VIEW"),
    Table("TABLE");
}

/**
 * Description: Provides a set of methods for creating a DB object.
 */
interface CreationAdapter {
    /**
     * @return The query used to create this table.
     */
    val creationQuery: String

    val name: String

    val type: ObjectType

    /**
     * @return When false, this table gets generated and associated with database, however it will not immediately
     * get created upon startup. This is useful for keeping around legacy tables for migrations.
     */
    fun createWithDatabase(): Boolean = true
}

/**
 * Runs the creation query on the DB.
 */
fun CreationAdapter.createIfNotExists(wrapper: DatabaseWrapper) {
    wrapper.compileStatement(creationQuery).use {
        it.execute()
    }
}

/**
 * Drops the table by running a drop query.
 */
fun CreationAdapter.drop(wrapper: DatabaseWrapper, ifExists: Boolean = false) {
    wrapper.compileStatement("DROP ${type.value} $name ${if (ifExists) "IF EXISTS" else ""};").use {
        it.execute()
    }
}
