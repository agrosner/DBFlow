package com.dbflow5.database

import android.database.Cursor
import android.database.CursorWrapper
import com.dbflow5.database.FlowCursor.Companion.COLUMN_NOT_FOUND
import java.io.Closeable

/**
 * A low level DB Cursor that reads row by row from a query and returns values based on index. It
 * should be rare you need to use this class directly unless you need to
 * perform a custom data retrieval.
 */
interface FlowCursor : Closeable, Iterable<FlowCursor> {
    fun isNull(index: Int): Boolean
    fun getColumnIndex(columnName: String): Int
    fun moveToFirst(): Boolean
    fun moveToNext(): Boolean
    val size: Int
    override fun iterator(): Iterator<FlowCursor> = FlowCursorIterator(this)

    fun getString(index: Int, defValue: String = ""): String
    fun getStringOrNull(index: Int, defValue: String? = null): String?
    fun getInt(index: Int, defValue: Int = 0): Int
    fun getIntOrNull(index: Int, defValue: Int? = null): Int?
    fun getDouble(index: Int, defValue: Double = 0.0): Double
    fun getDoubleOrNull(index: Int, defValue: Double? = null): Double?
    fun getFloat(index: Int, defValue: Float = 0f): Float
    fun getFloatOrNull(index: Int, defValue: Float? = null): Float?
    fun getLong(index: Int, defValue: Long = 0L): Long
    fun getLongOrNull(index: Int, defValue: Long? = null): Long?
    fun getShort(index: Int, defValue: Short = 0): Short
    fun getShortOrNull(index: Int, defValue: Short? = null): Short?
    fun getBlob(index: Int, defValue: ByteArray = byteArrayOf()): ByteArray
    fun getBlobOrNull(index: Int, defValue: ByteArray? = null): ByteArray?
    fun getBoolean(index: Int, defValue: Boolean = false): Boolean
    fun getBooleanOrNull(index: Int, defValue: Boolean? = null): Boolean?

    companion object {
        const val COLUMN_NOT_FOUND = -1
    }
}

/**
 * Basic implementation that enables [Iterator] behavior.
 */
class FlowCursorIterator(private val cursor: FlowCursor) : Iterator<FlowCursor> {
    private var movedToFirst = false
    override fun hasNext(): Boolean = cursor.moveToNext()

    override fun next(): FlowCursor {
        synchronized(movedToFirst) {
            if (!movedToFirst) {
                cursor.moveToFirst()
                movedToFirst = true
            }
        }
        return cursor
    }
}

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
        return if (index != COLUMN_NOT_FOUND && !isNull(index)) {
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
