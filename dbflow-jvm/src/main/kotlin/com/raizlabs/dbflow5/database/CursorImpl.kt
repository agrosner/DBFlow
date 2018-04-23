package com.raizlabs.dbflow5.database

import com.almworks.sqlite4java.SQLiteException
import com.almworks.sqlite4java.SQLiteStatement
import java.io.Closeable

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

private const val INVALID_ROW = -1

/**
 * Provides wrapping on [Cursor] for compatibility into DBFlow internal api.
 */
class AWCursor internal constructor(private val statement: SQLiteStatement,
                                    rowCount: Int) : Cursor {

    override val count: Int = rowCount

    private var position = INVALID_ROW

    override fun isNull(index: Int): Boolean {
        return statement.columnNull(index)
    }

    override fun getColumnIndex(columnName: String): Int = try {
        statement.getBindParameterIndex(columnName)
    } catch (s: SQLiteException) {
        //FlowLog.logError(s) // should we log this?
        INVALID_ROW
    }

    override fun getString(index: Int): String = statement.columnString(index)

    override fun getInt(index: Int): Int = statement.columnInt(index)

    override fun getDouble(index: Int): Double = statement.columnDouble(index)

    override fun getFloat(index: Int): Float = statement.columnDouble(index).toFloat()

    override fun getLong(index: Int): Long = statement.columnLong(index)

    override fun getShort(index: Int): Short = statement.columnInt(index).toShort()

    override fun getBlob(index: Int): ByteArray = statement.columnBlob(index)

    override fun moveToFirst(): Boolean {
        if (statement.hasStepped()) {
            statement.reset(false)
        }
        return moveToNext()
    }

    override fun moveToNext(): Boolean = try {
        position++
        statement.step()
    } catch (e: SQLiteException) {
        position = INVALID_ROW
        false
    }

    override fun moveToPosition(index: Int): Boolean {
        var retVal = position > INVALID_ROW && position < count
        if (index < position) {
            this.position = INVALID_ROW
        }
        while (retVal && index > position) {
            retVal = if (position < 0) moveToFirst() else moveToNext()
        }
        return retVal
    }

    override fun isClosed(): Boolean = statement.isDisposed

    override fun close() = statement.dispose()

    companion object {

        @JvmStatic
        fun from(sqLiteStatement: SQLiteStatement): Cursor = AWCursor(sqLiteStatement)
    }
}
