package com.raizlabs.android.dbflow.structure.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.os.Build

/**
 * Description: Specifies the android default implementation of a database.
 */
class AndroidDatabase internal constructor(val database: SQLiteDatabase) : DatabaseWrapper {

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

    override val version: Int
        get() = database.version

    override fun compileStatement(rawQuery: String): DatabaseStatement =
            AndroidDatabaseStatement.from(database.compileStatement(rawQuery), database)

    override fun rawQuery(query: String, selectionArgs: Array<String>?): FlowCursor =
            FlowCursor.from(database.rawQuery(query, selectionArgs))

    override fun updateWithOnConflict(tableName: String,
                                      contentValues: ContentValues,
                                      where: String?,
                                      whereArgs: Array<String>?,
                                      conflictAlgorithm: Int): Long =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                database.updateWithOnConflict(tableName, contentValues, where, whereArgs, conflictAlgorithm).toLong()
            } else {
                database.update(tableName, contentValues, where, whereArgs).toLong()
            }

    override fun insertWithOnConflict(tableName: String,
                                      nullColumnHack: String?,
                                      values: ContentValues,
                                      sqLiteDatabaseAlgorithmInt: Int): Long =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                database.insertWithOnConflict(tableName, nullColumnHack, values, sqLiteDatabaseAlgorithmInt)
            } else {
                database.insert(tableName, nullColumnHack, values)
            }

    override fun query(tableName: String,
                       columns: Array<String>?,
                       selection: String?,
                       selectionArgs: Array<String>?,
                       groupBy: String?,
                       having: String?,
                       orderBy: String?): FlowCursor =
            FlowCursor.from(database.query(tableName, columns, selection, selectionArgs, groupBy, having, orderBy))

    override fun delete(tableName: String, whereClause: String?, whereArgs: Array<String>?): Int =
            database.delete(tableName, whereClause, whereArgs)

    companion object {

        @JvmStatic
        fun from(database: SQLiteDatabase): AndroidDatabase = AndroidDatabase(database)
    }
}
