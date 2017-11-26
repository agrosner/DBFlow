package com.raizlabs.android.dbflow.sqlcipher

import android.content.ContentValues

import com.raizlabs.android.dbflow.structure.database.DatabaseStatement
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper
import com.raizlabs.android.dbflow.structure.database.FlowCursor

import net.sqlcipher.database.SQLiteDatabase

/**
 * Description: Implements the code necessary to use a [SQLiteDatabase] in dbflow.
 */
class SQLCipherDatabase
internal constructor(val database: SQLiteDatabase) : DatabaseWrapper {

    override val version: Int
        get() = database.version

    override fun execSQL(query: String) {
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

    override fun compileStatement(rawQuery: String): DatabaseStatement =
            SQLCipherStatement.from(database.compileStatement(rawQuery))

    override fun rawQuery(query: String, selectionArgs: Array<String>?): FlowCursor =
            FlowCursor.from(database.rawQuery(query, selectionArgs))

    override fun updateWithOnConflict(tableName: String,
                                      contentValues: ContentValues,
                                      where: String?, whereArgs: Array<String>?,
                                      conflictAlgorithm: Int): Long =
            database.updateWithOnConflict(tableName, contentValues,
                    where, whereArgs, conflictAlgorithm).toLong()

    override fun insertWithOnConflict(tableName: String, nullColumnHack: String?, values: ContentValues, sqLiteDatabaseAlgorithmInt: Int): Long {
        return database.insertWithOnConflict(tableName, nullColumnHack, values, sqLiteDatabaseAlgorithmInt)
    }

    override fun query(tableName: String,
                       columns: Array<String>?,
                       selection: String?,
                       selectionArgs: Array<String>?,
                       groupBy: String?,
                       having: String?,
                       orderBy: String?): FlowCursor {
        return FlowCursor.from(database.query(tableName, columns, selection, selectionArgs, groupBy, having, orderBy))
    }

    override fun delete(tableName: String, whereClause: String?, whereArgs: Array<String>?): Int {
        return database.delete(tableName, whereClause, whereArgs)
    }

    companion object {

        @JvmStatic
        fun from(database: SQLiteDatabase): SQLCipherDatabase = SQLCipherDatabase(database)
    }
}
