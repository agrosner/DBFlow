@file:JvmName("SqlUtils")

package com.raizlabs.dbflow5

import android.content.ContentValues
import android.net.Uri
import com.raizlabs.dbflow5.config.FlowManager
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.query.NameAlias
import com.raizlabs.dbflow5.query.Operator
import com.raizlabs.dbflow5.query.OperatorGroup
import com.raizlabs.dbflow5.query.SQLOperator
import com.raizlabs.dbflow5.structure.ChangeAction
import com.raizlabs.dbflow5.structure.Model

/**
 * Description: Provides some handy methods for dealing with SQL statements. It's purpose is to move the
 * methods away from the [Model] class and let any class use these.
 */
private val hexArray = "0123456789ABCDEF".toCharArray()

val TABLE_QUERY_PARAM = "tableName"

/**
 * Constructs a [Uri] from a set of [SQLOperator] for specific table.
 *
 * @param modelClass The class of table,
 * @param action     The action to use.
 * @param conditions The set of key-value [SQLOperator] to construct into a uri.
 * @return The [Uri].
 */
fun getNotificationUri(contentAuthority: String,
                       modelClass: Class<*>,
                       action: ChangeAction?,
                       conditions: Iterable<SQLOperator>?): Uri {
    val uriBuilder = Uri.Builder().scheme("dbflow")
        .authority(contentAuthority)
        .appendQueryParameter(TABLE_QUERY_PARAM, FlowManager.getTableName(modelClass))
    if (action != null) {
        uriBuilder.fragment(action.name)
    }
    if (conditions != null) {
        for (condition in conditions) {
            uriBuilder.appendQueryParameter(Uri.encode(condition.columnName()), Uri.encode(condition.value().toString()))
        }
    }
    return uriBuilder.build()
}


/**
 * Constructs a [Uri] from a set of [SQLOperator] for specific table.
 *
 * @param modelClass The class of table,
 * @param action     The action to use.
 * @param conditions The set of key-value [SQLOperator] to construct into a uri.
 * @return The [Uri].
 */
fun getNotificationUri(contentAuthority: String,
                       modelClass: Class<*>,
                       action: ChangeAction?,
                       conditions: Array<SQLOperator>?): Uri {
    val uriBuilder = Uri.Builder().scheme("dbflow")
        .authority(contentAuthority)
        .appendQueryParameter(TABLE_QUERY_PARAM, FlowManager.getTableName(modelClass))
    action?.let { uriBuilder.fragment(action.name) }
    if (conditions != null && conditions.isNotEmpty()) {
        for (condition in conditions) {
            uriBuilder.appendQueryParameter(Uri.encode(condition.columnName()),
                Uri.encode(condition.value().toString()))
        }
    }
    return uriBuilder.build()
}

/**
 * Returns the uri for notifications from model changes
 *
 * @param modelClass  The class to get table from.
 * @param action      the action changed.
 * @param notifyKey   The column key.
 * @param notifyValue The column value that gets turned into a String.
 * @return Notification uri.
 */
@JvmOverloads
fun getNotificationUri(contentAuthority: String,
                       modelClass: Class<*>,
                       action: ChangeAction?,
                       notifyKey: String = "",
                       notifyValue: Any? = null): Uri {
    var operator: Operator<Any>? = null
    if (notifyKey.isNotNullOrEmpty()) {
        operator = Operator.op<Any>(NameAlias.Builder(notifyKey).build()).value(notifyValue)
    }
    return getNotificationUri(contentAuthority, modelClass, action,
        if (operator != null) arrayOf<SQLOperator>(operator) else null)
}


/**
 * Drops an active TRIGGER by specifying the onTable and triggerName
 *
 * @param mOnTable    The table that this trigger runs on
 * @param triggerName The name of the trigger
 */
fun dropTrigger(mOnTable: Class<*>, triggerName: String) {
    FlowManager.getDatabaseForTable(mOnTable).execSQL("DROP TRIGGER IF EXISTS " + triggerName)
}

/**
 * Drops an active INDEX by specifying the onTable and indexName
 *
 * @param indexName The name of the index.
 */
fun dropIndex(databaseWrapper: DatabaseWrapper, indexName: String) {
    databaseWrapper.execSQL("DROP INDEX IF EXISTS " + indexName.quoteIfNeeded()!!)
}

fun dropIndex(onTable: Class<*>, indexName: String) {
    dropIndex(FlowManager.getDatabaseForTable(onTable), indexName)
}

/**
 * Adds [ContentValues] to the specified [OperatorGroup].
 *
 * @param contentValues The content values to convert.
 * @param operatorGroup The group to put them into as [Operator].
 */
fun addContentValues(contentValues: ContentValues, operatorGroup: OperatorGroup) {
    val entries = contentValues.valueSet()

    for ((key) in entries) {
        operatorGroup.and(Operator.op<Any>(NameAlias.Builder(key).build())
            .`is`(contentValues.get(key)))
    }
}

/**
 * @param contentValues The object to check existence of.
 * @param key           The key to check.
 * @return The key, whether it's quoted or not.
 */
fun getContentValuesKey(contentValues: ContentValues, key: String): String {
    val quoted = key.quoteIfNeeded()
    return when {
        contentValues.containsKey(quoted) -> quoted ?: ""
        else -> {
            val stripped = key.stripQuotes()
            when {
                contentValues.containsKey(stripped) -> stripped ?: ""
                else -> throw IllegalArgumentException("Could not find the specified key in the Content Values object.")
            }
        }
    }
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
    return String(hexChars)
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
