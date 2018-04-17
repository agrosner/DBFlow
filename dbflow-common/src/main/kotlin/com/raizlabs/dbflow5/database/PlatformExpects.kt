package com.raizlabs.dbflow5.database

import com.raizlabs.dbflow5.Closeable

expect interface Cursor : Closeable {

    fun isNull(index: Int): Boolean

    fun getColumnIndex(columnName: String): Int

    fun getString(index: Int): String

    fun getInt(index: Int): Int

    fun getDouble(index: Int): Double

    fun getFloat(index: Int): Float

    fun getLong(index: Int): Long

    fun getShort(index: Int): Short

    fun getBlob(index: Int): ByteArray

    fun moveToFirst(): Boolean

    fun moveToNext(): Boolean

    fun moveToPosition(index: Int): Boolean

    fun isClosed(): Boolean
}

expect val Cursor.count: Int

open class CursorWrapper(private val cursor: Cursor) : Cursor by cursor {

    open fun getWrappedCursor(): Cursor = cursor
}
