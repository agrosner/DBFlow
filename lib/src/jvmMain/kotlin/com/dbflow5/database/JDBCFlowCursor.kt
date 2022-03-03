package com.dbflow5.database

import java.sql.ResultSet

class JDBCFlowCursor(private val resultSet: ResultSet) : FlowCursor {
    override fun isNull(index: Int): Boolean = resultSet.getObject(index) == null

    override fun getColumnIndex(columnName: String): Int = resultSet.findColumn(columnName)

    override fun moveToFirst(): Boolean = resultSet.first()

    override fun moveToNext(): Boolean = resultSet.next()

    override val size: Int = resultSet.fetchSize

    override fun getString(index: Int, defValue: String): String =
        getOrDefault(index, defValue) { resultSet.getString(index) }

    override fun getStringOrNull(index: Int, defValue: String?): String? =
        getOrDefault(index, defValue) { resultSet.getString(index) }

    override fun getInt(index: Int, defValue: Int): Int =
        getOrDefault(index, defValue) { resultSet.getInt(index) }

    override fun getIntOrNull(index: Int, defValue: Int?): Int? =
        getOrDefault(index, defValue) { resultSet.getInt(index) }

    override fun getDouble(index: Int, defValue: Double): Double =
        getOrDefault(index, defValue) { resultSet.getDouble(index) }

    override fun getDoubleOrNull(index: Int, defValue: Double?): Double? =
        getOrDefault(index, defValue) { resultSet.getDouble(index) }

    override fun getFloat(index: Int, defValue: Float): Float =
        getOrDefault(index, defValue) { resultSet.getFloat(index) }

    override fun getFloatOrNull(index: Int, defValue: Float?): Float? =
        getOrDefault(index, defValue) { resultSet.getFloat(index) }

    override fun getLong(index: Int, defValue: Long): Long =
        getOrDefault(index, defValue) { resultSet.getLong(index) }

    override fun getLongOrNull(index: Int, defValue: Long?): Long? =
        getOrDefault(index, defValue) { resultSet.getLong(index) }

    override fun getShort(index: Int, defValue: Short): Short =
        getOrDefault(index, defValue) { resultSet.getShort(index) }

    override fun getShortOrNull(index: Int, defValue: Short?): Short? =
        getOrDefault(index, defValue) { resultSet.getShort(index) }

    override fun getBlob(index: Int, defValue: ByteArray): ByteArray =
        getOrDefault(index, defValue) { resultSet.getBytes(index) }

    override fun getBlobOrNull(index: Int, defValue: ByteArray?): ByteArray? =
        getOrDefault(index, defValue) { resultSet.getBytes(index) }

    override fun getBoolean(index: Int, defValue: Boolean): Boolean =
        getOrDefault(index, defValue) { resultSet.getBoolean(index) }

    override fun getBooleanOrNull(index: Int, defValue: Boolean?): Boolean? =
        getOrDefault(index, defValue) { resultSet.getBoolean(index) }

    override fun close() {
        resultSet.close()
    }
}
