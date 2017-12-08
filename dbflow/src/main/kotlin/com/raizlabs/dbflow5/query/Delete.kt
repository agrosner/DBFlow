package com.raizlabs.dbflow5.query

import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.sql.Query
import kotlin.reflect.KClass

/**
 * Description: Constructs the beginning of a SQL DELETE query
 */
class Delete internal constructor(private val databaseWrapper: DatabaseWrapper) : Query {

    override val query: String
        get() = "DELETE "

    /**
     * Returns the new SQL FROM statement wrapper
     *
     * @param table    The table we want to run this query from
     * @param [T] The table class
     * @return [T]
     **/
    infix fun <T : Any> from(table: Class<T>): From<T> = From(databaseWrapper, this, table)

    infix fun <T : Any> from(table: KClass<T>): From<T> = from(table.java)


    companion object {

        @JvmStatic
        fun <T : Any> DatabaseWrapper.delete(modelClass: KClass<T>) = delete(modelClass.java)

        @JvmStatic
        inline fun <reified T : Any> DatabaseWrapper.delete() = delete(T::class)

        /**
         * Deletes the specified table
         *
         * @param table      The table to delete
         * @param conditions The list of conditions to use to delete from the specified table
         * @param [T]   The class that implements [com.raizlabs.android.dbflow.structure.Model]
         */
        @JvmStatic
        fun <T : Any> DatabaseWrapper.table(table: Class<T>, vararg conditions: SQLOperator) {
            Delete(this).from(table).where(*conditions).executeUpdateDelete()
        }

        /**
         * Deletes the list of tables specified.
         * WARNING: this will completely clear all rows from each table.
         *
         * @param tables The list of tables to wipe.
         */
        @JvmStatic
        fun DatabaseWrapper.tables(vararg tables: Class<*>) {
            tables.forEach { table(it) }
        }
    }
}

