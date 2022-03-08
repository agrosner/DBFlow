package com.dbflow5.database

import java.sql.ResultSet

/**
 * Implements using the [FlowCursor] interface wrapper.
 *
 * Since JDBC is "1" index based, we adjust to account for that.
 */
class JDBCFlowCursor(private val resultSet: ResultSet) : FlowCursor {

    override fun isNull(index: Int): Boolean = resultSet.getObject(index + 1) == null

    override fun getColumnIndex(columnName: String): Int = resultSet.findColumn(columnName) - 1

    override fun moveToFirst(): Boolean = true

    override fun moveToNext(): Boolean = resultSet.next()

    override val size: Int = resultSet.fetchSize

    override fun getString(index: Int, defValue: String): String =
        getOrDefault(index, defValue) { resultSet.getString(index + 1) }

    override fun getStringOrNull(index: Int, defValue: String?): String? =
        getOrDefault(index, defValue) { resultSet.getString(index + 1) }

    override fun getInt(index: Int, defValue: Int): Int =
        getOrDefault(index, defValue) { resultSet.getInt(index + 1) }

    override fun getIntOrNull(index: Int, defValue: Int?): Int? =
        getOrDefault(index, defValue) { resultSet.getInt(index + 1) }

    override fun getDouble(index: Int, defValue: Double): Double =
        getOrDefault(index, defValue) { resultSet.getDouble(index + 1) }

    override fun getDoubleOrNull(index: Int, defValue: Double?): Double? =
        getOrDefault(index, defValue) { resultSet.getDouble(index + 1) }

    override fun getFloat(index: Int, defValue: Float): Float =
        getOrDefault(index, defValue) { resultSet.getFloat(index + 1) }

    override fun getFloatOrNull(index: Int, defValue: Float?): Float? =
        getOrDefault(index, defValue) { resultSet.getFloat(index + 1) }

    override fun getLong(index: Int, defValue: Long): Long =
        getOrDefault(index, defValue) { resultSet.getLong(index + 1) }

    override fun getLongOrNull(index: Int, defValue: Long?): Long? =
        getOrDefault(index, defValue) { resultSet.getLong(index + 1) }

    override fun getShort(index: Int, defValue: Short): Short =
        getOrDefault(index, defValue) { resultSet.getShort(index + 1) }

    override fun getShortOrNull(index: Int, defValue: Short?): Short? =
        getOrDefault(index, defValue) { resultSet.getShort(index + 1) }

    override fun getBlob(index: Int, defValue: ByteArray): ByteArray =
        getOrDefault(index, defValue) { resultSet.getBytes(index + 1) }

    override fun getBlobOrNull(index: Int, defValue: ByteArray?): ByteArray? =
        getOrDefault(index, defValue) { resultSet.getBytes(index + 1) }

    override fun getBoolean(index: Int, defValue: Boolean): Boolean =
        getOrDefault(index, defValue) { resultSet.getBoolean(index + 1) }

    override fun getBooleanOrNull(index: Int, defValue: Boolean?): Boolean? =
        getOrDefault(index, defValue) { resultSet.getBoolean(index + 1) }

    override fun close() {
        resultSet.close()
    }
}
