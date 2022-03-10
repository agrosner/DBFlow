package com.dbflow5.database

import com.dbflow5.mpp.Closeable
import kotlinx.atomicfu.atomic

/**
 * A low level DB Cursor that reads row by row from a query and returns values based on index. It
 * should be rare you need to use this class directly unless you need to
 * perform a custom data retrieval.
 *
 * Indexes are 0-based. Some platforms like JVM JDBC use an index of "1" :( so we account
 * for that internally.
 */
interface FlowCursor : Closeable, Iterable<FlowCursor> {
    fun isNull(index: Int): Boolean
    fun getColumnIndex(columnName: String): Int
    fun moveToFirst(): Boolean
    fun moveToNext(): Boolean
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

inline fun <T> FlowCursor.getOrDefault(
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


/**
 * Basic implementation that enables [Iterator] behavior.
 */
class FlowCursorIterator(private val cursor: FlowCursor) : Iterator<FlowCursor> {
    private var movedToFirst by atomic(false)
    override fun hasNext(): Boolean = cursor.moveToNext()

    override fun next(): FlowCursor {
        if (!movedToFirst) {
            cursor.moveToFirst()
            movedToFirst = true
        }
        return cursor
    }
}
