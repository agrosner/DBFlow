package com.raizlabs.dbflow5.android.support

import android.arch.persistence.db.SupportSQLiteDatabase
import android.content.ContentValues
import com.raizlabs.dbflow5.database.DatabaseStatement
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.database.FlowCursor
import com.raizlabs.dbflow5.database.rethrowDBFlowException

/**
 * Description: Species the [SupportSQLiteDatabase] implementation of DBFlow. This provides compatibility
 * to the support libraries.
 */
class SupportAndroidDatabase internal constructor(val database: SupportSQLiteDatabase) : DatabaseWrapper {
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

    override fun compileStatement(rawQuery: String): DatabaseStatement
        = rethrowDBFlowException { SupportAndroidDatabaseStatement.from(database.compileStatement(rawQuery)) }

    override fun rawQuery(query: String, selectionArgs: Array<String>?): FlowCursor
        = rethrowDBFlowException { FlowCursor.from(database.query(query, selectionArgs)) }

    override fun updateWithOnConflict(tableName: String,
                                      contentValues: ContentValues,
                                      where: String?,
                                      whereArgs: Array<String>?,
                                      conflictAlgorithm: Int): Long =
        rethrowDBFlowException {
            database.update(tableName, conflictAlgorithm, contentValues, where, whereArgs)
        }.toLong()

    override fun insertWithOnConflict(tableName: String,
                                      nullColumnHack: String?,
                                      values: ContentValues,
                                      sqLiteDatabaseAlgorithmInt: Int): Long =
        rethrowDBFlowException { database.insert(tableName, sqLiteDatabaseAlgorithmInt, values) }

    override fun delete(tableName: String, whereClause: String?, whereArgs: Array<String>?): Int =
        rethrowDBFlowException { database.delete(tableName, whereClause, whereArgs) }

    companion object {

        @JvmStatic
        fun from(database: SupportSQLiteDatabase): SupportAndroidDatabase = SupportAndroidDatabase(database)
    }
}