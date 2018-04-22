package com.raizlabs.dbflow5.database

import java.io.Closeable
import java.sql.ResultSet

actual interface Cursor : Closeable {
    actual fun isNull(index: Int): Boolean
    actual fun getColumnIndex(columnName: String): Int
    actual fun getString(index: Int): String
    actual fun getInt(index: Int): Int
    actual fun getDouble(index: Int): Double
    actual fun getFloat(index: Int): Float
    actual fun getLong(index: Int): Long
    actual fun getShort(index: Int): Short
    actual fun getBlob(index: Int): ByteArray
    actual fun moveToFirst(): Boolean
    actual fun moveToNext(): Boolean
    actual fun moveToPosition(index: Int): Boolean
    actual fun isClosed(): Boolean
}

class JDBCCursor(private val resultSet: ResultSet) : Cursor {

    override fun isNull(index: Int): Boolean {
        return resultSet.wasNull()
    }
}