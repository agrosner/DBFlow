package com.raizlabs.android.dbflow.query

import com.raizlabs.android.dbflow.isNotNullOrEmpty
import com.raizlabs.android.dbflow.sql.Query

/**
 * Description: This class will use a String to describe its condition.
 * Not recommended for normal queries, but can be used as a fall-back.
 */
class UnSafeStringOperator(selection: String, selectionArgs: Array<String>) : SQLOperator, Query {

    private val conditionString: String?
    private var separator = ""

    override val query: String
        get() = appendToQuery()

    init {
        var newSelection: String? = selection
        // replace question marks in order
        if (newSelection != null) {
            for (selectionArg in selectionArgs) {
                newSelection = newSelection?.replaceFirst("\\?".toRegex(), selectionArg)
            }
        }
        this.conditionString = newSelection
    }

    override fun appendConditionToQuery(queryBuilder: StringBuilder) {
        queryBuilder.append(conditionString)
    }

    override fun columnName(): String = ""

    override fun separator(): String? = separator

    override fun separator(separator: String) = apply {
        this.separator = separator
    }

    override fun hasSeparator(): Boolean = separator.isNotNullOrEmpty()

    override fun operation(): String = ""

    override fun value(): Any? = ""
}