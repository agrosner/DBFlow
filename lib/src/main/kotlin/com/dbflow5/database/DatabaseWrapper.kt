package com.dbflow5.database

/**
 * Description: Provides a base implementation that wraps a database, so other databaseForTable engines potentially can
 * be used.
 */
interface DatabaseWrapper {

    val isInTransaction: Boolean

    /**
     * The current version of the database.
     */
    val version: Int

    /**
     * Execute an arbitrary SQL query.
     */
    suspend fun execSQL(query: String)

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
    suspend fun rawQuery(query: String, selectionArgs: Array<String>?): FlowCursor

    fun query(tableName: String, columns: Array<String>?, selection: String?,
              selectionArgs: Array<String>?, groupBy: String?,
              having: String?, orderBy: String?): FlowCursor

    fun delete(tableName: String, whereClause: String?, whereArgs: Array<String>?): Int

}

inline fun DatabaseWrapper.executeTransaction(dbFn: DatabaseWrapper.() -> Unit) {
    try {
        beginTransaction()
        dbFn()
        setTransactionSuccessful()
    } finally {
        endTransaction()
    }
}

