@file:JvmName("SqlUtils")

package com.dbflow5

import com.dbflow5.database.DatabaseConnection
import com.dbflow5.mpp.use
import com.dbflow5.structure.Model
import kotlin.jvm.JvmName

/**
 * Description: Provides some handy methods for dealing with SQL statements. It's purpose is to move the
 * methods away from the [Model] class and let any class use these.
 */
private val hexArray = "0123456789ABCDEF".toCharArray()

val TABLE_QUERY_PARAM = "tableName"

/**
 * Drops an active INDEX by specifying the onTable and indexName
 *
 * @param indexName The name of the index.
 */
fun dropIndex(databaseConnection: DatabaseConnection, indexName: String) {
    databaseConnection.execute("DROP INDEX IF EXISTS ${indexName.quoteIfNeeded()}")
}

fun longForQuery(wrapper: DatabaseConnection, query: String): Long =
    wrapper.compileStatement(query).use { statement -> statement.simpleQueryForLong() }

fun stringForQuery(wrapper: DatabaseConnection, query: String): String? =
    wrapper.compileStatement(query).use { statement -> statement.simpleQueryForString() }

fun doubleForQuery(wrapper: DatabaseConnection, query: String): Double =
    wrapper.compileStatement(query).use { statement -> statement.simpleQueryForLong().toDouble() }

/**
 * Converts a byte[] to a String hex representation for within wrapper queries.
 */
fun byteArrayToHexString(bytes: ByteArray?): String {
    if (bytes == null) return ""
    val hexChars = CharArray(bytes.size * 2)
    for (j in bytes.indices) {
        val v = bytes[j].toInt() and 0xFF
        hexChars[j * 2] = hexArray[v.ushr(4)]
        hexChars[j * 2 + 1] = hexArray[v and 0x0F]
    }
    return hexChars.concatToString()
}

fun sqlEscapeString(value: String): String = buildString { appendEscapedSQLString(this, value) }

fun appendEscapedSQLString(sb: StringBuilder, sqlString: String) {
    sb.apply {
        append('\'')
        if (sqlString.indexOf('\'') != -1) {
            val length = sqlString.length
            for (i in 0 until length) {
                val c = sqlString[i]
                if (c == '\'') {
                    append('\'')
                }
                append(c)
            }
        } else
            append(sqlString)
        append('\'')
    }
}
