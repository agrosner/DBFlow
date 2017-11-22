package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.sql.Query
import com.raizlabs.android.dbflow.sql.QueryBuilder

/**
 * Description: Constructs the beginning of a SQL DELETE query
 */
class Delete : Query {

    override val query: String
        get() = QueryBuilder()
                .append("DELETE")
                .appendSpace().query

    /**
     * Returns the new SQL FROM statement wrapper
     *
     * @param table    The table we want to run this query from
     * @param <TModel> The table class
     * @return <TModel>
     **/
    fun <TModel> from(table: Class<TModel>): From<TModel> = From(this, table)

    companion object {

        /**
         * Deletes the specified table
         *
         * @param table      The table to delete
         * @param conditions The list of conditions to use to delete from the specified table
         * @param <TModel>   The class that implements [com.raizlabs.android.dbflow.structure.Model]
        </TModel> */
        fun <TModel> table(table: Class<TModel>, vararg conditions: SQLOperator) {
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
