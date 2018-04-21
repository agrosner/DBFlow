package com.raizlabs.dbflow5.database

import android.content.ContentValues
import kotlin.reflect.KClass

/**
 * Description:
 */
actual abstract class DBFlowDatabase : InternalDBFlowDatabase() {

    abstract val associatedDatabaseClassFile: Class<*>

    override val associatedDatabaseKClassFile: KClass<*>
        get() = associatedDatabaseClassFile.kotlin

    override fun updateWithOnConflict(tableName: String,
                                      contentValues: ContentValues,
                                      where: String?,
                                      whereArgs: Array<String>?,
                                      conflictAlgorithm: Int): Long = writableDatabase.updateWithOnConflict(tableName, contentValues, where, whereArgs, conflictAlgorithm)

    override fun insertWithOnConflict(
        tableName: String,
        nullColumnHack: String?,
        values: ContentValues,
        sqLiteDatabaseAlgorithmInt: Int): Long = writableDatabase.insertWithOnConflict(tableName, nullColumnHack, values, sqLiteDatabaseAlgorithmInt)

}