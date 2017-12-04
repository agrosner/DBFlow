package com.raizlabs.dbflow5.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.os.Build

/**
 * Description: Specifies the android default implementation of a database.
 */
class AndroidDatabase internal constructor(val database: SQLiteDatabase) : DatabaseWrapper {

    override fun execSQL(query: String) {
        try {
            database.execSQL(query)
        } catch (e: SQLiteException) {
            throw e.toSqliteException()
        }
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

    override fun compileStatement(rawQuery: String): DatabaseStatement =
        AndroidDatabaseStatement.from(database.compileStatement(rawQuery), database)

    override fun rawQuery(query: String, selectionArgs: Array<String>?): FlowCursor = try {
        FlowCursor.from(database.rawQuery(query, selectionArgs))
    } catch (e: SQLiteException) {
        throw e.toSqliteException()
    }

    override fun updateWithOnConflict(tableName: String,
                                      contentValues: ContentValues,
                                      where: String?,
                                      whereArgs: Array<String>?,
                                      conflictAlgorithm: Int): Long = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            database.updateWithOnConflict(tableName, contentValues, where, whereArgs, conflictAlgorithm).toLong()
        } else {
            database.update(tableName, contentValues, where, whereArgs).toLong()
        }
    } catch (e: SQLiteException) {
        throw e.toSqliteException()
    }

    override fun insertWithOnConflict(tableName: String,
                                      nullColumnHack: String?,
                                      values: ContentValues,
                                      sqLiteDatabaseAlgorithmInt: Int): Long = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            database.insertWithOnConflict(tableName, nullColumnHack, values, sqLiteDatabaseAlgorithmInt)
        } else {
            database.insert(tableName, nullColumnHack, values)
        }
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
        fun from(database: SQLiteDatabase): AndroidDatabase = AndroidDatabase(database)
    }
}

fun SQLiteException.toSqliteException() = com.raizlabs.dbflow5.database.SQLiteException("A Database Error Occurred", this)