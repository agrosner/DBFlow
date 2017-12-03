package com.raizlabs.dbflow5.database

import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement
import android.os.Build

/**
 * Description:
 */
class AndroidDatabaseStatement
internal constructor(val statement: SQLiteStatement,
                     private val database: SQLiteDatabase) : BaseDatabaseStatement() {

    override fun executeUpdateDelete(): Long {
        var count: Long = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            count = statement.executeUpdateDelete().toLong()
        } else {
            statement.execute()

            var cursor: Cursor? = null
            try {
                cursor = database.rawQuery("SELECT changes() AS affected_row_count", null)
                if (cursor != null && cursor.count > 0 && cursor.moveToFirst()) {
                    count = cursor.getLong(cursor.getColumnIndex("affected_row_count"))
                }
            } catch (e: SQLException) {
                // Handle exception here.
            } finally {
                if (cursor != null) {
                    cursor.close()
                }
            }
        }
        return count
    }

    override fun execute() {
        statement.execute()
    }

    override fun close() {
        statement.close()
    }

    override fun simpleQueryForLong(): Long = statement.simpleQueryForLong()

    override fun simpleQueryForString(): String? = statement.simpleQueryForString()

    override fun executeInsert(): Long = statement.executeInsert()

    override fun bindString(index: Int, s: String) {
        statement.bindString(index, s)
    }

    override fun bindNull(index: Int) {
        statement.bindNull(index)
    }

    override fun bindLong(index: Int, aLong: Long) {
        statement.bindLong(index, aLong)
    }

    override fun bindDouble(index: Int, aDouble: Double) {
        statement.bindDouble(index, aDouble)
    }

    override fun bindBlob(index: Int, bytes: ByteArray) {
        statement.bindBlob(index, bytes)
    }

    companion object {

        @JvmStatic
        fun from(sqLiteStatement: SQLiteStatement,
                 database: SQLiteDatabase): AndroidDatabaseStatement =
                AndroidDatabaseStatement(sqLiteStatement, database)
    }
}
