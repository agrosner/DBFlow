package com.raizlabs.android.dbflow.database

import android.content.ContentValues

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

    fun rawQuery(query: String, selectionArgs: Array<String>?): FlowCursor

    fun updateWithOnConflict(tableName: String,
                             contentValues: ContentValues,
                             where: String?,
                             whereArgs: Array<String>?, conflictAlgorithm: Int): Long

    fun insertWithOnConflict(tableName: String,
                             nullColumnHack: String?,
                             values: ContentValues,
                             sqLiteDatabaseAlgorithmInt: Int): Long

    fun query(tableName: String, columns: Array<String>?, selection: String?,
              selectionArgs: Array<String>?, groupBy: String?,
              having: String?, orderBy: String?): FlowCursor

    fun delete(tableName: String, whereClause: String?, whereArgs: Array<String>?): Int
}
