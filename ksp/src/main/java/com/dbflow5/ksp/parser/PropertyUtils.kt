package com.dbflow5.ksp.parser

import com.google.devtools.ksp.symbol.KSValueArgument

/**
 * Description:
 */


inline fun <reified T> Map<String, KSValueArgument>.arg(name: String): T {
    return getValue(name).value as T
}

fun List<KSValueArgument>.mapProperties() = associateBy { it.name!!.getShortName() }
