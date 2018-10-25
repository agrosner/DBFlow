package com.dbflow5.database

import android.content.ContentValues

/**
 * Description:
 */
interface AndroidDatabaseWrapper : DatabaseWrapper {


    fun updateWithOnConflict(tableName: String,
                             contentValues: ContentValues,
                             where: String?,
                             whereArgs: Array<String>?, conflictAlgorithm: Int): Long

    fun insertWithOnConflict(tableName: String,
                             nullColumnHack: String?,
                             values: ContentValues,
                             sqLiteDatabaseAlgorithmInt: Int): Long

}