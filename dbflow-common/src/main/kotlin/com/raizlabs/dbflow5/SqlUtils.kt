@file:JvmName("SqlUtils")

package com.raizlabs.dbflow5

import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.structure.Model
import kotlin.jvm.JvmName

/**
 * Description: Provides some handy methods for dealing with SQL statements. It's purpose is to move the
 * methods away from the [Model] class and let any class use these.
 */
private val hexArray: CharArray = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

val TABLE_QUERY_PARAM = "tableName"

/**
 * Drops an active TRIGGER by specifying the onTable and triggerName
 *
 * @param mOnTable    The table that this trigger runs on
 * @param triggerName The name of the trigger
 */
fun dropTrigger(databaseWrapper: DatabaseWrapper, triggerName: String) {
    databaseWrapper.execSQL("DROP TRIGGER IF EXISTS $triggerName")
}

/**
 * Drops an active INDEX by specifying the onTable and indexName
 *
 * @param indexName The name of the index.
 */
fun dropIndex(databaseWrapper: DatabaseWrapper, indexName: String) {
    databaseWrapper.execSQL("DROP INDEX IF EXISTS ${indexName.quoteIfNeeded()}")
}

fun longForQuery(wrapper: DatabaseWrapper, query: String): Long =
    wrapper.compileStatement(query).let { statement ->
        try {
            statement.simpleQueryForLong()
        } finally {
            statement.close()
        }
    }

fun doubleForQuery(wrapper: DatabaseWrapper, query: String): Double =
    wrapper.compileStatement(query).let { statement ->
        try {
            statement.simpleQueryForLong().toDouble()
        } finally {
            statement.close()
        }
    }

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
    return hexChars.joinToString()
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
