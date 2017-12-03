package com.raizlabs.dbflow5.database

import android.database.Cursor
import android.database.CursorWrapper

/**
 * Common [Cursor] class that wraps cursors we use in this library with convenience loading methods.
 * This is used to help cut down on generated code size and potentially decrease method count.
 */
class FlowCursor private constructor(private val cursor: Cursor) : CursorWrapper(cursor) {

    // compatibility
    override fun getWrappedCursor(): Cursor = cursor

    fun getStringOrDefault(index: Int, defValue: String): String {
        return if (index != -1 && !cursor.isNull(index)) {
            cursor.getString(index)
        } else {
            defValue
        }
    }

    fun getStringOrDefault(columnName: String): String? =
            getStringOrDefault(cursor.getColumnIndex(columnName))

    fun getStringOrDefault(index: Int): String? {
        return if (index != -1 && !cursor.isNull(index)) {
            cursor.getString(index)
        } else {
            null
        }
    }

    fun getStringOrDefault(columnName: String, defValue: String): String =
            getStringOrDefault(cursor.getColumnIndex(columnName), defValue)

    fun getIntOrDefault(columnName: String): Int =
            getIntOrDefault(cursor.getColumnIndex(columnName))

    fun getIntOrDefault(index: Int): Int {
        return if (index != -1 && !cursor.isNull(index)) {
            cursor.getInt(index)
        } else {
            0
        }
    }

    fun getIntOrDefault(index: Int, defValue: Int): Int {
        return if (index != -1 && !cursor.isNull(index)) {
            cursor.getInt(index)
        } else {
            defValue
        }
    }

    fun getIntOrDefault(columnName: String, defValue: Int): Int =
            getIntOrDefault(cursor.getColumnIndex(columnName), defValue)

    fun getIntOrDefault(index: Int, defValue: Int?): Int? {
        return if (index != -1 && !cursor.isNull(index)) {
            cursor.getInt(index)
        } else {
            defValue
        }
    }

    fun getIntOrDefault(columnName: String, defValue: Int?): Int? =
            getIntOrDefault(cursor.getColumnIndex(columnName), defValue)

    fun getDoubleOrDefault(index: Int, defValue: Double): Double {
        return if (index != -1 && !cursor.isNull(index)) {
            cursor.getDouble(index)
        } else {
            defValue
        }
    }

    fun getDoubleOrDefault(columnName: String): Double =
            getDoubleOrDefault(cursor.getColumnIndex(columnName))

    fun getDoubleOrDefault(index: Int): Double {
        return if (index != -1 && !cursor.isNull(index)) {
            cursor.getDouble(index)
        } else {
            0.0
        }
    }

    fun getDoubleOrDefault(columnName: String, defValue: Double): Double =
            getDoubleOrDefault(cursor.getColumnIndex(columnName), defValue)

    fun getDoubleOrDefault(index: Int, defValue: Double?): Double? {
        return if (index != -1 && !cursor.isNull(index)) {
            cursor.getDouble(index)
        } else {
            defValue
        }
    }

    fun getDoubleOrDefault(columnName: String, defValue: Double?): Double? =
            getDoubleOrDefault(cursor.getColumnIndex(columnName), defValue)

    fun getFloatOrDefault(index: Int, defValue: Float): Float {
        return if (index != -1 && !cursor.isNull(index)) {
            cursor.getFloat(index)
        } else {
            defValue
        }
    }

    fun getFloatOrDefault(columnName: String): Float =
            getFloatOrDefault(cursor.getColumnIndex(columnName))

    fun getFloatOrDefault(index: Int): Float {
        return if (index != -1 && !cursor.isNull(index)) {
            cursor.getFloat(index)
        } else {
            0f
        }
    }

    fun getFloatOrDefault(columnName: String, defValue: Float): Float =
            getFloatOrDefault(cursor.getColumnIndex(columnName), defValue)

    fun getFloatOrDefault(index: Int, defValue: Float?): Float? {
        return if (index != -1 && !cursor.isNull(index)) {
            cursor.getFloat(index)
        } else {
            defValue
        }
    }

    fun getFloatOrDefault(columnName: String, defValue: Float?): Float? =
            getFloatOrDefault(cursor.getColumnIndex(columnName), defValue)

    fun getLongOrDefault(index: Int, defValue: Long): Long {
        return if (index != -1 && !cursor.isNull(index)) {
            cursor.getLong(index)
        } else {
            defValue
        }
    }

    fun getLongOrDefault(columnName: String): Long =
            getLongOrDefault(cursor.getColumnIndex(columnName))

    fun getLongOrDefault(index: Int): Long {
        return if (index != -1 && !cursor.isNull(index)) {
            cursor.getLong(index)
        } else {
            0
        }
    }

    fun getLongOrDefault(columnName: String, defValue: Long): Long =
            getLongOrDefault(cursor.getColumnIndex(columnName), defValue)

    fun getLongOrDefault(index: Int, defValue: Long?): Long? {
        return if (index != -1 && !cursor.isNull(index)) {
            cursor.getLong(index)
        } else {
            defValue
        }
    }

    fun getLongOrDefault(columnName: String, defValue: Long?): Long? =
            getLongOrDefault(cursor.getColumnIndex(columnName), defValue)

    fun getShortOrDefault(index: Int, defValue: Short): Short {
        return if (index != -1 && !cursor.isNull(index)) {
            cursor.getShort(index)
        } else {
            defValue
        }
    }

    fun getShortOrDefault(columnName: String): Short =
            getShortOrDefault(cursor.getColumnIndex(columnName))

    fun getShortOrDefault(index: Int): Short {
        return if (index != -1 && !cursor.isNull(index)) {
            cursor.getShort(index)
        } else {
            0
        }
    }

    fun getShortOrDefault(columnName: String, defValue: Short): Short =
            getShortOrDefault(cursor.getColumnIndex(columnName), defValue)

    fun getShortOrDefault(index: Int, defValue: Short?): Short? {
        return if (index != -1 && !cursor.isNull(index)) {
            cursor.getShort(index)
        } else {
            defValue
        }
    }

    fun getShortOrDefault(columnName: String, defValue: Short?): Short? =
            getShortOrDefault(cursor.getColumnIndex(columnName), defValue)

    fun getBlobOrDefault(columnName: String): ByteArray? =
            getBlobOrDefault(cursor.getColumnIndex(columnName))

    fun getBlobOrDefault(index: Int): ByteArray? {
        return if (index != -1 && !cursor.isNull(index)) {
            cursor.getBlob(index)
        } else {
            null
        }
    }

    fun getBlobOrDefault(index: Int, defValue: ByteArray): ByteArray {
        return if (index != -1 && !cursor.isNull(index)) {
            cursor.getBlob(index)
        } else {
            defValue
        }
    }

    fun getBlobOrDefault(columnName: String, defValue: ByteArray): ByteArray =
            getBlobOrDefault(cursor.getColumnIndex(columnName), defValue)

    fun getBooleanOrDefault(index: Int, defValue: Boolean): Boolean {
        return if (index != -1 && !cursor.isNull(index)) {
            getBoolean(index)
        } else {
            defValue
        }
    }

    fun getBooleanOrDefault(columnName: String): Boolean =
            getBooleanOrDefault(cursor.getColumnIndex(columnName))

    fun getBooleanOrDefault(index: Int): Boolean {
        return if (index != -1 && !cursor.isNull(index)) {
            getBoolean(index)
        } else {
            false
        }
    }

    fun getBooleanOrDefault(columnName: String, defValue: Boolean): Boolean =
            getBooleanOrDefault(cursor.getColumnIndex(columnName), defValue)

    fun getBoolean(index: Int): Boolean = cursor.getInt(index) == 1

    companion object {

        @JvmStatic
        fun from(cursor: Cursor): FlowCursor = cursor as? FlowCursor ?: FlowCursor(cursor)
    }

}

