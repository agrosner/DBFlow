@file:JvmName("StringUtils")

package com.raizlabs.dbflow5

import com.raizlabs.dbflow5.sql.SQLiteType
import java.util.regex.Pattern


/**
 * @return true if the string is not null, empty string "", or the length is greater than 0
 */
fun String?.isNullOrEmpty(): Boolean =
    this == null || this == "" || isEmpty()

/**
 * @return true if the string is null, empty string "", or the length is less than equal to 0
 */
fun String?.isNotNullOrEmpty(): Boolean =
    this != null && this != "" && isNotEmpty()

fun StringBuilder.appendQuotedIfNeeded(string: String?) = apply {
    if (string == "*")
        return append(string)

    append(string.quoteIfNeeded())
    return this
}

private val QUOTE = '`'
private val QUOTE_PATTERN: Pattern = (QUOTE + ".*" + QUOTE).toPattern()

/**
 * Helper method to check if name is quoted.
 *
 * @return true if the name is quoted. We may not want to quote something if its already so.
 */
fun String?.isQuoted(): Boolean = QUOTE_PATTERN.matcher(this).find()

/**
 * @param columnName The column name to use.
 * @return A name in quotes. E.G. index =&gt; `index` so we can use keywords as column names without fear
 * of clashing.
 */
fun String?.quote(): String = "${QUOTE}${this?.replace(".", "`.`")}${QUOTE}"

fun String?.quoteIfNeeded() = if (this != null && !isQuoted()) {
    quote()
} else {
    this
}

/**
 * Appends the [SQLiteType] to [StringBuilder]
 */
fun StringBuilder.appendSQLiteType(sqLiteType: SQLiteType): StringBuilder = append(sqLiteType.name)

/**
 * Strips quotes out of a identifier if need to do so.
 *
 * @param name The name ot strip the quotes from.
 * @return A non-quoted name.
 */
fun String?.stripQuotes(): String? {
    var ret: String? = this
    if (ret != null && ret.isQuoted()) {
        ret = ret.replace("`", "")
    }
    return ret
}


/**
 * Appends a value only if it's not empty or null
 *
 * @param name  The name of the qualifier
 * @param value The value to append after the name
 * @return This instance
 */
fun StringBuilder.appendQualifier(name: String?, value: String?): StringBuilder {
    if (value != null && value.isNotEmpty()) {
        if (name != null) {
            append(name)
        }
        append(" $value ")
    }
    return this
}

/**
 * Appends the value only if its not null
 *
 * @param value If not null, its string representation.
 * @return This instance
 */
fun StringBuilder.appendOptional(value: Any?): StringBuilder {
    if (value != null) {
        append(value)
    }
    return this
}

/**
 * Appends an array of these objects by joining them with a comma with
 * [.join]
 *
 * @param objects The array of objects to pass in
 * @return This instance
 */
fun StringBuilder.appendArray(vararg objects: Any): StringBuilder = append(objects.joinToString())

/**
 * Appends a list of objects by joining them with a comma with
 * [.join]
 *
 * @param objects The list of objects to pass in
 * @return This instance
 */
fun StringBuilder.appendList(objects: List<*>): StringBuilder = append(objects.joinToString())
