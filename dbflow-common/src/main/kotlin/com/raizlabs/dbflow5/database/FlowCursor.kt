package com.raizlabs.dbflow5.database

import com.raizlabs.dbflow5.JvmOverloads
import com.raizlabs.dbflow5.JvmStatic

/**
 * Common [Cursor] class that wraps cursors we use in this library with convenience loading methods.
 * This is used to help cut down on generated code size and potentially decrease method count.
 */
class FlowCursor private constructor(private val cursor: Cursor) : CursorWrapper(cursor) {

    // compatibility
    override fun getWrappedCursor(): Cursor = cursor

    @JvmOverloads
    fun getStringOrDefault(index: Int, defValue: String = ""): String = getValue(index, defValue) { cursor.getString(index) }

    @JvmOverloads
    fun getStringOrDefault(columnName: String, defValue: String = ""): String =
        getStringOrDefault(cursor.getColumnIndex(columnName), defValue)

    @JvmOverloads
    fun getIntOrDefault(index: Int, defValue: Int = 0): Int = getValue(index, defValue) { cursor.getInt(index) }

    @JvmOverloads
    fun getIntOrDefault(columnName: String, defValue: Int = 0): Int =
        getIntOrDefault(cursor.getColumnIndex(columnName), defValue)

    @JvmOverloads
    fun getIntOrDefault(index: Int, defValue: Int? = null): Int? =
        getValue(index, defValue) { cursor.getInt(index) }

    @JvmOverloads
    fun getIntOrDefault(columnName: String, defValue: Int? = null): Int? =
        getIntOrDefault(cursor.getColumnIndex(columnName), defValue)

    @JvmOverloads
    fun getDoubleOrDefault(index: Int, defValue: Double = 0.0): Double =
        getValue(index, defValue) { cursor.getDouble(index) }

    @JvmOverloads
    fun getDoubleOrDefault(columnName: String, defValue: Double = 0.0): Double =
        getDoubleOrDefault(cursor.getColumnIndex(columnName), defValue)

    @JvmOverloads
    fun getDoubleOrDefault(index: Int, defValue: Double? = null): Double? =
        getValue(index, defValue) { cursor.getDouble(index) }

    @JvmOverloads
    fun getDoubleOrDefault(columnName: String, defValue: Double? = null): Double? =
        getDoubleOrDefault(cursor.getColumnIndex(columnName), defValue)

    @JvmOverloads
    fun getFloatOrDefault(index: Int, defValue: Float = 0f): Float =
        getValue(index, defValue) { cursor.getFloat(index) }

    @JvmOverloads
    fun getFloatOrDefault(columnName: String, defValue: Float = 0f): Float =
        getFloatOrDefault(cursor.getColumnIndex(columnName), defValue)

    @JvmOverloads
    fun getFloatOrDefault(index: Int, defValue: Float? = null): Float? =
        getValue(index, defValue) { cursor.getFloat(index) }

    fun getFloatOrDefault(columnName: String, defValue: Float?): Float? =
        getFloatOrDefault(cursor.getColumnIndex(columnName), defValue)

    @JvmOverloads
    fun getLongOrDefault(index: Int, defValue: Long = 0): Long =
        getValue(index, defValue) { cursor.getLong(index) }

    @JvmOverloads
    fun getLongOrDefault(columnName: String, defValue: Long = 0): Long =
        getLongOrDefault(cursor.getColumnIndex(columnName), defValue)

    @JvmOverloads
    fun getLongOrDefault(index: Int, defValue: Long? = null): Long? =
        getValue(index, defValue) { cursor.getLong(index) }

    @JvmOverloads
    fun getLongOrDefault(columnName: String, defValue: Long? = null): Long? =
        getLongOrDefault(cursor.getColumnIndex(columnName), defValue)

    @JvmOverloads
    fun getShortOrDefault(index: Int, defValue: Short = 0): Short =
        getValue(index, defValue) { cursor.getShort(index) }

    @JvmOverloads
    fun getShortOrDefault(columnName: String, defValue: Short = 0): Short =
        getShortOrDefault(cursor.getColumnIndex(columnName), defValue)

    @JvmOverloads
    fun getShortOrDefault(index: Int, defValue: Short? = null): Short? =
        getValue(index, defValue) { cursor.getShort(index) }

    @JvmOverloads
    fun getShortOrDefault(columnName: String, defValue: Short? = null): Short? =
        getShortOrDefault(cursor.getColumnIndex(columnName), defValue)

    @JvmOverloads
    fun getBlobOrDefault(index: Int, defValue: ByteArray? = null): ByteArray? =
        getValue(index, defValue) { cursor.getBlob(index) }

    @JvmOverloads
    fun getBlobOrDefault(columnName: String, defValue: ByteArray? = null): ByteArray? =
        getBlobOrDefault(cursor.getColumnIndex(columnName), defValue)

    @JvmOverloads
    fun getBooleanOrDefault(index: Int, defValue: Boolean = false): Boolean =
        getValue(index, defValue) { getBoolean(index) }

    @JvmOverloads
    fun getBooleanOrDefault(columnName: String, defValue: Boolean = false): Boolean =
        getBooleanOrDefault(cursor.getColumnIndex(columnName), defValue)

    fun getBoolean(index: Int): Boolean = cursor.getInt(index) == 1

    companion object {

        @JvmStatic
        fun from(cursor: Cursor): FlowCursor = cursor as? FlowCursor ?: FlowCursor(cursor)
    }

}

