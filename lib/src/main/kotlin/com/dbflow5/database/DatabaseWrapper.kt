package com.dbflow5.database

import com.dbflow5.config.GeneratedDatabase

/**
 * Description: Provides a base implementation that wraps a database, so other databaseForTable engines potentially can
 * be used.
 */
interface DatabaseWrapper {

    val generatedDatabase: GeneratedDatabase

    val isInTransaction: Boolean

    /**
     * The current version of the database.
     */
    val version: Int

    /**
     * Execute an arbitrary SQL query.
     */
    fun execSQL(query: String)

    /**
     * Begin a transaction.
     */
    fun beginTransaction()

    /**
     * Set when a transaction complete successfully to preserve db state.
     */
    fun setTransactionSuccessful()

    /**
     * Always called whenever transaction should complete. If [setTransactionSuccessful] is not called,
     * db state will not be preserved.
     */
    fun endTransaction()

    /**
     * For a given query, return a [DatabaseStatement].
     */
    fun compileStatement(rawQuery: String): DatabaseStatement

    /**
     * For a given query and selectionArgs, return a [DatabaseStatement].
     */
    fun compileStatement(rawQuery: String, selectionArgs: Array<String>?): DatabaseStatement {
        return compileStatement(rawQuery).apply { bindAllArgsAsStrings(selectionArgs) }
    }

    /**
     * For given query and selection args, return a [FlowCursor] to retrieve data.
     */
    fun rawQuery(query: String, selectionArgs: Array<String>?): FlowCursor

    fun query(
        tableName: String, columns: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, groupBy: String?,
        having: String?, orderBy: String?
    ): FlowCursor

    fun delete(tableName: String, whereClause: String?, whereArgs: Array<String>?): Int

}

inline fun <R> DatabaseWrapper.executeTransaction(dbFn: DatabaseWrapper.() -> R): R {
    try {
        beginTransaction()
        val result = dbFn()
        setTransactionSuccessful()
        return result
    } finally {
        endTransaction()
    }
}

