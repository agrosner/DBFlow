package com.raizlabs.dbflow5.sqlcipher

import android.content.ContentValues
import com.raizlabs.dbflow5.database.DatabaseStatement
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.database.FlowCursor
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteException

/**
 * Description: Implements the code necessary to use a [SQLiteDatabase] in dbflow.
 */
class SQLCipherDatabase
internal constructor(val database: SQLiteDatabase) : DatabaseWrapper {

    override val version: Int
        get() = database.version

    override fun execSQL(query: String) = try {
        database.execSQL(query)
    } catch (e: SQLiteException) {
        throw e.toSqliteException()
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

    override fun compileStatement(rawQuery: String): DatabaseStatement = try {
        SQLCipherStatement.from(database.compileStatement(rawQuery))
    } catch (e: SQLiteException) {
        throw e.toSqliteException()
    }

    override fun rawQuery(query: String, selectionArgs: Array<String>?): FlowCursor = try {
        FlowCursor.from(database.rawQuery(query, selectionArgs))
    } catch (e: SQLiteException) {
        throw e.toSqliteException()
    }

    override fun updateWithOnConflict(tableName: String,
                                      contentValues: ContentValues,
                                      where: String?, whereArgs: Array<String>?,
                                      conflictAlgorithm: Int): Long = try {
        database.updateWithOnConflict(tableName, contentValues,
            where, whereArgs, conflictAlgorithm).toLong()
    } catch (e: SQLiteException) {
        throw e.toSqliteException()
    }

    override fun insertWithOnConflict(tableName: String, nullColumnHack: String?, values: ContentValues, sqLiteDatabaseAlgorithmInt: Int): Long = try {
        database.insertWithOnConflict(tableName, nullColumnHack, values, sqLiteDatabaseAlgorithmInt)
    } catch (e: SQLiteException) {
        throw e.toSqliteException()
    }

    override fun query(tableName: String,
                       columns: Array<String>?,
                       selection: String?,
                       selectionArgs: Array<String>?,
                       groupBy: String?,
                       having: String?,
                       orderBy: String?): FlowCursor = try {
        FlowCursor.from(database.query(tableName, columns, selection, selectionArgs, groupBy, having, orderBy))
    } catch (e: SQLiteException) {
        throw e.toSqliteException()
    }

    override fun delete(tableName: String, whereClause: String?, whereArgs: Array<String>?): Int = try {
        database.delete(tableName, whereClause, whereArgs)
    } catch (e: SQLiteException) {
        throw e.toSqliteException()
    }

    companion object {

        @JvmStatic
        fun from(database: SQLiteDatabase): SQLCipherDatabase = SQLCipherDatabase(database)
    }
}

fun SQLiteException.toSqliteException() = com.raizlabs.dbflow5.database.SQLiteException("A Database Error Occurred", this)