package com.dbflow5.database

import android.database.Cursor
import android.database.CursorWrapper

/**
 * Android wrapper that calls into a [Cursor].
 */
class AndroidFlowCursor(private val cursor: Cursor) : CursorWrapper(cursor),
    FlowCursor {

    // compatibility
    override fun getWrappedCursor(): Cursor = cursor

    override val size: Int = cursor.count

    private inline fun <T> FlowCursor.getOrDefault(
        index: Int,
        defValue: T,
        getValue: FlowCursor.() -> T,
    ): T {
        return if (index != FlowCursor.COLUMN_NOT_FOUND && !isNull(index)) {
            this.getValue()
        } else {
            defValue
        }
    }

    override fun getString(index: Int, defValue: String): String =
        getOrDefault(index, defValue) { cursor.getString(index) }

    override fun getStringOrNull(index: Int, defValue: String?): String? =
        getOrDefault(index, defValue) { cursor.getString(index) }

    override fun getInt(index: Int, defValue: Int): Int =
        getOrDefault(index, defValue) { cursor.getInt(index) }

    override fun getIntOrNull(index: Int, defValue: Int?): Int? =
        getOrDefault(index, defValue) { cursor.getInt(index) }

    override fun getDouble(index: Int, defValue: Double): Double =
        getOrDefault(index, defValue) { cursor.getDouble(index) }

    override fun getDoubleOrNull(index: Int, defValue: Double?): Double? =
        getOrDefault(index, defValue) { cursor.getDouble(index) }

    override fun getFloat(index: Int, defValue: Float): Float =
        getOrDefault(index, defValue) { cursor.getFloat(index) }

    override fun getFloatOrNull(index: Int, defValue: Float?): Float? =
        getOrDefault(index, defValue) { cursor.getFloat(index) }

    override fun getLong(index: Int, defValue: Long): Long =
        getOrDefault(index, defValue) { cursor.getLong(index) }

    override fun getLongOrNull(index: Int, defValue: Long?): Long? =
        getOrDefault(index, defValue) { cursor.getLong(index) }

    override fun getShort(index: Int, defValue: Short): Short =
        getOrDefault(index, defValue) { cursor.getShort(index) }

    override fun getShortOrNull(index: Int, defValue: Short?): Short? =
        getOrDefault(index, defValue) { cursor.getShort(index) }

    override fun getBlob(index: Int, defValue: ByteArray): ByteArray =
        getOrDefault(index, defValue) { cursor.getBlob(index) }

    override fun getBlobOrNull(index: Int, defValue: ByteArray?): ByteArray? =
        getOrDefault(index, defValue) { cursor.getBlob(index) }

    override fun getBoolean(index: Int, defValue: Boolean): Boolean =
        getOrDefault(index, defValue) { cursor.getInt(index) == 1 }

    override fun getBooleanOrNull(index: Int, defValue: Boolean?): Boolean? =
        getOrDefault(index, defValue) { cursor.getInt(index) == 1 }
}
