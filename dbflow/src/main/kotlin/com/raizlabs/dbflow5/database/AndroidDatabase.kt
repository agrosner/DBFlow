package com.raizlabs.dbflow5.database

import android.database.sqlite.SQLiteDatabase

/**
 * Description: Specifies the android default implementation of a database.
 */
class AndroidDatabase internal constructor(val database: SQLiteDatabase) : DatabaseWrapper {

    override fun execSQL(query: String) {
        rethrowDBFlowException { database.execSQL(query) }
    }

    override fun beginTransaction() {
        database.beginTransaction()
    }

    override fun setTransactionSuccessful() {
        database.setTransactionSuccessful()
    }

    override fun endTransaction() {
        database.endTransaction()
    }

    override val version: Int
        get() = database.version

    override fun compileStatement(rawQuery: String): DatabaseStatement = rethrowDBFlowException {
        AndroidDatabaseStatement.from(database.compileStatement(rawQuery), database)
    }

    override fun rawQuery(query: String, selectionArgs: Array<String>?): FlowCursor = rethrowDBFlowException {
        FlowCursor.from(database.rawQuery(query, selectionArgs))
    }

    override fun query(tableName: String,
                       columns: Array<String>?,
                       selection: String?,
                       selectionArgs: Array<String>?,
                       groupBy: String?,
                       having: String?,
                       orderBy: String?): FlowCursor = rethrowDBFlowException {
        FlowCursor.from(database.query(tableName, columns, selection, selectionArgs, groupBy, having, orderBy))
    }

    override fun delete(tableName: String, whereClause: String?, whereArgs: Array<String>?): Int = rethrowDBFlowException {
        database.delete(tableName, whereClause, whereArgs)
    }

    companion object {

        @JvmStatic
        fun from(database: SQLiteDatabase): AndroidDatabase = AndroidDatabase(database)
    }
}

fun com.raizlabs.dbflow5.database.SQLiteException.toDBFlowSQLiteException() = com.raizlabs.dbflow5.database.SQLiteException("A Database Error Occurred", this)

inline fun <T> rethrowDBFlowException(fn: () -> T) = try {
    fn()
} catch (e: com.raizlabs.dbflow5.database.SQLiteException) {
    throw e.toDBFlowSQLiteException()
}