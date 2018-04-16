package com.raizlabs.dbflow5.database

/**
 * Description: Provides a base implementation that wraps a database, so other databaseForTable engines potentially can
 * be used.
 */
interface DatabaseWrapper {

    val version: Int

    fun execSQL(query: String)

    fun beginTransaction()

    fun setTransactionSuccessful()

    fun endTransaction()

    fun compileStatement(rawQuery: String): DatabaseStatement

    fun compileStatement(rawQuery: String, selectionArgs: Array<String>?): DatabaseStatement {
        return compileStatement(rawQuery).apply { bindAllArgsAsStrings(selectionArgs) }
    }

    fun rawQuery(query: String, selectionArgs: Array<String>?): FlowCursor

    fun query(tableName: String, columns: Array<String>?, selection: String?,
              selectionArgs: Array<String>?, groupBy: String?,
              having: String?, orderBy: String?): FlowCursor

    fun delete(tableName: String, whereClause: String?, whereArgs: Array<String>?): Int
}
