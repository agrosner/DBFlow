@file:JvmName("StringUtils")

package com.dbflow5

import kotlin.jvm.JvmName


fun String?.isNotNullOrEmpty(): Boolean = !isNullOrEmpty()

fun StringBuilder.appendQuotedIfNeeded(string: String?) = apply {
    if (string == "*")
        return append(string)

    append(string.quoteIfNeeded())
    return this
}

private val QUOTE = '`'
private val QUOTE_PATTERN: Regex = ("$QUOTE.*$QUOTE").toRegex()

/**
 * Helper method to check if name is quoted.
 *
 * @return true if the name is quoted. We may not want to quote something if its already so.
 */
fun String?.isQuoted(): Boolean = this?.let { QUOTE_PATTERN.matches(this) } ?: false

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

@JvmName("quoteIfNeededNonNull")
fun String.quoteIfNeeded() = if (!isQuoted()) {
    quote()
} else {
    this
}


/**
 * Strips quotes out of a identifier if need to do so.
 *
 * @param name The name ot strip the quotes from.
 * @return A non-quoted name.
 */
fun String?.stripQuotes(): String? =
    if (this?.isQuoted() == true) {
        replace("`", "")
    } else this

@JvmName("stripQuotesNotNull")
fun String.stripQuotes(): String = if (isQuoted()) {
    replace("`", "")
} else this
