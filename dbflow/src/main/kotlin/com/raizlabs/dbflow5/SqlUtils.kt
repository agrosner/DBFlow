package com.raizlabs.dbflow5

import android.content.ContentValues
import android.net.Uri
import com.raizlabs.dbflow5.config.FlowManager
import com.raizlabs.dbflow5.query.NameAlias
import com.raizlabs.dbflow5.query.Operator
import com.raizlabs.dbflow5.query.OperatorGroup
import com.raizlabs.dbflow5.query.SQLOperator
import com.raizlabs.dbflow5.structure.ChangeAction
import kotlin.jvm.JvmOverloads
import kotlin.reflect.KClass

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
