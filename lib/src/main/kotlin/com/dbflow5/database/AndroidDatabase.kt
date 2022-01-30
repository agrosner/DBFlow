package com.dbflow5.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import com.dbflow5.config.DBFlowDatabase

/**
 * Description: Specifies the android default implementation of a database.
 */
class AndroidDatabase internal constructor(
    val database: SQLiteDatabase,
    override val associatedDBFlowDatabase: DBFlowDatabase,
) : AndroidDatabaseWrapper {

    override fun execSQL(query: String) {
        rethrowDBFlowException { database.execSQL(query) }
    }

    override val isInTransaction: Boolean
        get() = database.inTransaction()

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

    override fun rawQuery(query: String, selectionArgs: Array<String>?): FlowCursor =
        rethrowDBFlowException {
            FlowCursor.from(database.rawQuery(query, selectionArgs))
        }

    override fun updateWithOnConflict(
        tableName: String,
        contentValues: ContentValues,
        where: String?,
        whereArgs: Array<String>?,
        conflictAlgorithm: Int
    ): Long = rethrowDBFlowException {
        database.updateWithOnConflict(tableName, contentValues, where, whereArgs, conflictAlgorithm)
            .toLong()
    }

    override fun insertWithOnConflict(
        tableName: String,
        nullColumnHack: String?,
        values: ContentValues,
        sqLiteDatabaseAlgorithmInt: Int
    ): Long = rethrowDBFlowException {
        database.insertWithOnConflict(tableName, nullColumnHack, values, sqLiteDatabaseAlgorithmInt)
    }

    override fun query(
        tableName: String,
        columns: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        groupBy: String?,
        having: String?,
        orderBy: String?
    ): FlowCursor = rethrowDBFlowException {
        FlowCursor.from(
            database.query(
                tableName,
                columns,
                selection,
                selectionArgs,
                groupBy,
                having,
                orderBy
            )
        )
    }

    override fun delete(tableName: String, whereClause: String?, whereArgs: Array<String>?): Int =
        rethrowDBFlowException {
            database.delete(tableName, whereClause, whereArgs)
        }

    companion object {

        @JvmStatic
        fun from(
            database: SQLiteDatabase,
            dbFlowDatabase: DBFlowDatabase
        ): AndroidDatabase = AndroidDatabase(
            database,
            dbFlowDatabase,
        )
    }
}

fun SQLiteException.toDBFlowSQLiteException() =
    com.dbflow5.database.SQLiteException("A Database Error Occurred", this)

inline fun <T> rethrowDBFlowException(fn: () -> T) = try {
    fn()
} catch (e: SQLiteException) {
    throw e.toDBFlowSQLiteException()
}