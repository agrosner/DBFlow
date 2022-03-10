package com.dbflow5.database

import co.touchlab.sqliter.Cursor
import co.touchlab.sqliter.getColumnIndexOrThrow
import co.touchlab.sqliter.getLongOrNull

/**
 * Description:
 */
class NativeFlowCursor(
    private val cursor: Cursor,
) : FlowCursor {
    override fun isNull(index: Int): Boolean = cursor.isNull(index)

    override fun getColumnIndex(columnName: String): Int = cursor.getColumnIndexOrThrow(columnName)

    override fun moveToFirst(): Boolean = true

    override fun moveToNext(): Boolean = cursor.next()

    override fun getString(index: Int, defValue: String): String =
        getOrDefault(index, defValue) { cursor.getString(index) }

    override fun getStringOrNull(index: Int, defValue: String?): String? =
        getOrDefault(index, defValue) { cursor.getString(index) }

    override fun getInt(index: Int, defValue: Int): Int = getOrDefault(index, defValue) {
        cursor.getLong(index).toInt()
    }

    override fun getIntOrNull(index: Int, defValue: Int?): Int? = getOrDefault(index, defValue) {
        cursor.getLong(index).toInt()
    }

    override fun getDouble(index: Int, defValue: Double): Double = getOrDefault(index, defValue) {
        cursor.getDouble(index)
    }

    override fun getDoubleOrNull(index: Int, defValue: Double?): Double? =
        getOrDefault(index, defValue) {
            cursor.getDouble(index)
        }

    override fun getFloat(index: Int, defValue: Float): Float = getOrDefault(index, defValue) {
        cursor.getDouble(index).toFloat()
    }

    override fun getFloatOrNull(index: Int, defValue: Float?): Float? =
        getOrDefault(index, defValue) {
            cursor.getDouble(index).toFloat()
        }

    override fun getLong(index: Int, defValue: Long): Long = getOrDefault(index, defValue) {
        cursor.getLong(index)
    }

    override fun getLongOrNull(index: Int, defValue: Long?): Long? = getOrDefault(index, defValue) {
        cursor.getLong(index)
    }

    override fun getShort(index: Int, defValue: Short): Short = getOrDefault(index, defValue) {
        cursor.getLong(index).toShort()
    }

    override fun getShortOrNull(index: Int, defValue: Short?): Short? =
        getOrDefault(index, defValue) {
            cursor.getLong(index).toShort()
        }

    override fun getBlob(index: Int, defValue: ByteArray): ByteArray =
        getOrDefault(index, defValue) {
            cursor.getBytes(index)
        }

    override fun getBlobOrNull(index: Int, defValue: ByteArray?): ByteArray? =
        getOrDefault(index, defValue) {
            cursor.getBytes(index)
        }

    override fun getBoolean(index: Int, defValue: Boolean): Boolean =
        getOrDefault(index, defValue) {
            cursor.getLong(index) == 1L
        }

    override fun getBooleanOrNull(index: Int, defValue: Boolean?): Boolean? =
        getOrDefault(index, defValue) {
            cursor.getLongOrNull(index)?.let { it == 1L }
        }

    override fun close() {
        // Noop
    }
}