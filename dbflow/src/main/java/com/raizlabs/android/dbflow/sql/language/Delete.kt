package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.sql.Query

/**
 * Description: Constructs the beginning of a SQL DELETE query
 */
class Delete : Query {

    override val query: String
        get() = "DELETE "

    /**
     * Returns the new SQL FROM statement wrapper
     *
     * @param table    The table we want to run this query from
     * @param [T] The table class
     * @return [T]
     **/
    fun <T : Any> from(table: Class<T>): From<T> = From(this, table)

    companion object {

        /**
         * Deletes the specified table
         *
         * @param table      The table to delete
         * @param conditions The list of conditions to use to delete from the specified table
         * @param [T]   The class that implements [com.raizlabs.android.dbflow.structure.Model]
         */
        fun <T : Any> table(table: Class<T>, vararg conditions: SQLOperator) {
            Delete().from(table).where(*conditions).executeUpdateDelete()
        }

        /**
         * Deletes the list of tables specified.
         * WARNING: this will completely clear all rows from each table.
         *
         * @param tables The list of tables to wipe.
         */
        fun tables(vararg tables: Class<*>) {
            tables.forEach { table(it) }
        }
    }
}


inline fun <reified T : Any> delete() = SQLite.delete(T::class.java)

inline fun <reified T : Any> delete(deleteClause: From<T>.() -> BaseModelQueriable<T>)
        = deleteClause(SQLite.delete(T::class.java))