package com.raizlabs.dbflow5.query

import com.raizlabs.dbflow5.database.DatabaseWrapper
import kotlin.reflect.KClass

/**
 * Description: Jvm Implementation with extra from method.
 */
actual class Delete : InternalDelete() {

    fun <T : Any> from(table: Class<T>): From<T> = From(this, table)

    actual companion object {

        @JvmStatic
        actual fun <T : Any> delete(modelClass: KClass<T>) = InternalDelete.delete(modelClass)

        @JvmStatic
        fun <T : Any> delete(modelClass: Class<T>) = InternalDelete.delete(modelClass.kotlin)

        @JvmStatic
        actual
        inline fun <reified T : Any> delete() = InternalDelete.delete(T::class)

        /**
         * Deletes the specified table
         *
         * @param table      The table to delete
         * @param conditions The list of conditions to use to delete from the specified table
         * @param [T]   The class that implements [com.raizlabs.dbflow5.structure.Model]
         */
        @JvmStatic
        actual fun <T : Any> table(databaseWrapper: DatabaseWrapper,
                                   table: KClass<T>,
                                   vararg conditions: SQLOperator): Long =
            InternalDelete.table(databaseWrapper, table, *conditions)

        /**
         * Deletes the specified table
         *
         * @param table      The table to delete
         * @param conditions The list of conditions to use to delete from the specified table
         * @param [T]   The class that implements [com.raizlabs.dbflow5.structure.Model]
         */
        @JvmStatic
        fun <T : Any> table(databaseWrapper: DatabaseWrapper,
                            table: Class<T>,
                            vararg conditions: SQLOperator): Long =
            InternalDelete.table(databaseWrapper, table.kotlin, *conditions)

        /**
         * Deletes the list of tables specified.
         * WARNING: this will completely clear all rows from each table.
         *
         * @param tables The list of tables to wipe.
         */
        @JvmStatic
        actual fun tables(databaseWrapper: DatabaseWrapper, vararg tables: KClass<*>) = InternalDelete.tables(databaseWrapper, *tables)

        /**
         * Deletes the list of tables specified.
         * WARNING: this will completely clear all rows from each table.
         *
         * @param tables The list of tables to wipe.
         */
        @JvmStatic
        fun tables(databaseWrapper: DatabaseWrapper, vararg tables: Class<*>) = InternalDelete.tables(databaseWrapper,
            *tables.map { it.kotlin }.toTypedArray())
    }
}