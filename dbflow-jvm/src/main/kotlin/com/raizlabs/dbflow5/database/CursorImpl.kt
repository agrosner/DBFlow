package com.raizlabs.dbflow5.database

import java.io.Closeable
import java.sql.ResultSet
import java.sql.SQLException

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

    val count: Int
}

actual val Cursor.count: Int
    get() = this.count

/**
 * Implements the JDBC way of reading a cursor. Read row first, then check if null.
 */
actual inline fun <T> Cursor.getValue(index: Int, defaultValue: T, getter: () -> T): T {
    if (index == -1) return defaultValue

    // evaluate row first.
    val value = getter()
    return if (isNull(index)) defaultValue
    else value
}


/**
 * Provides wrapping on [Cursor] for compatibility into DBFlow internal api.
 */
class JDBCCursor(private val resultSet: ResultSet,
                 rowCount: Int) : Cursor {

    override val count: Int = rowCount

    override fun isNull(index: Int): Boolean {
        return false
    }

    override fun getColumnIndex(columnName: String): Int = try {
        resultSet.findColumn(columnName)
    } catch (s: SQLException) {
        //FlowLog.logError(s) // should we log this?
        -1
    }

    override fun getString(index: Int): String = resultSet.getString(index)

    override fun getInt(index: Int): Int = resultSet.getInt(index)

    override fun getDouble(index: Int): Double = resultSet.getDouble(index)

    override fun getFloat(index: Int): Float = resultSet.getFloat(index)

    override fun getLong(index: Int): Long = resultSet.getLong(index)

    override fun getShort(index: Int): Short = resultSet.getShort(index)

    override fun getBlob(index: Int): ByteArray = resultSet.getBytes(index)

    override fun moveToFirst(): Boolean = resultSet.first()

    override fun moveToNext(): Boolean = resultSet.next()

    override fun moveToPosition(index: Int): Boolean = resultSet.absolute(index)

    override fun isClosed(): Boolean = resultSet.isClosed

    override fun close() = resultSet.close()
}