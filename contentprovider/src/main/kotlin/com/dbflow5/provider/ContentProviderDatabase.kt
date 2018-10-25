package com.dbflow5.provider

import android.content.ContentValues
import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.database.AndroidDatabaseWrapper

/**
 * Description: Base class providing [ContentValues] for a [DBFlowDatabase].
 */
abstract class ContentProviderDatabase : AndroidDatabaseWrapper, DBFlowDatabase() {

    private val database
        get() = (writableDatabase as? AndroidDatabaseWrapper)
                ?: throw IllegalStateException("Invalid DB type used. It must be a type of ${AndroidDatabaseWrapper::class.java}. ")

    override fun updateWithOnConflict(tableName: String,
                                      contentValues: ContentValues,
                                      where: String?,
                                      whereArgs: Array<String>?,
                                      conflictAlgorithm: Int): Long =
            database.updateWithOnConflict(tableName, contentValues, where, whereArgs, conflictAlgorithm)

    override fun insertWithOnConflict(tableName: String,
                                      nullColumnHack: String?,
                                      values: ContentValues,
                                      sqLiteDatabaseAlgorithmInt: Int): Long =
            database.insertWithOnConflict(tableName, nullColumnHack, values, sqLiteDatabaseAlgorithmInt)
}