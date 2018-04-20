package com.raizlabs.dbflow5.sqlcipher

import android.content.ContentValues
import com.raizlabs.dbflow5.database.DatabaseWrapper
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteException
import com.raizlabs.dbflow5.database.DatabaseStatement
import com.raizlabs.dbflow5.database.FlowCursor

/**
 * Description: Implements the code necessary to use a [SQLiteDatabase] in dbflow.
 */
class SQLCipherDatabase
internal constructor(val database: SQLiteDatabase) : DatabaseWrapper {

    override val version: Int
        get() = database.version

    override fun execSQL(query: String) = rethrowDBFlowException {
        database.execSQL(query)
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

    override fun compileStatement(rawQuery: String): DatabaseStatement = rethrowDBFlowException {
        SQLCipherStatement.from(database.compileStatement(rawQuery))
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

    override fun delete(tableName: String,
                        whereClause: String?,
                        whereArgs: Array<String>?): Int = rethrowDBFlowException {
        database.delete(tableName, whereClause, whereArgs)
    }

    override fun updateWithOnConflict(tableName: String,
                                      contentValues: ContentValues,
                                      where: String?, whereArgs: Array<String>?,
                                      conflictAlgorithm: Int): Long = rethrowDBFlowException {
        database.updateWithOnConflict(tableName, contentValues, where, whereArgs, conflictAlgorithm).toLong()
    }

    override fun insertWithOnConflict(tableName: String,
                                      nullColumnHack: String?,
                                      values: ContentValues,
                                      sqLiteDatabaseAlgorithmInt: Int): Long = rethrowDBFlowException {
        database.insertWithOnConflict(tableName, nullColumnHack, values, sqLiteDatabaseAlgorithmInt)
    }

    companion object {

        @JvmStatic
        fun from(database: SQLiteDatabase): SQLCipherDatabase = SQLCipherDatabase(database)
    }
}

fun SQLiteException.toDBFlowSQLiteException() = com.raizlabs.dbflow5.database.SQLiteException("A Database Error Occurred", this)

inline fun <T> rethrowDBFlowException(fn: () -> T) = try {
    fn()
} catch (e: SQLiteException) {
    throw e.toDBFlowSQLiteException()
}
