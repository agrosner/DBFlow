package com.dbflow5.ksp.parser

import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueArgument

typealias ArgMap = Map<String, KSValueArgument>

inline fun <reified T> ArgMap.arg(name: String): T {
    return getValue(name).value as T
}

inline fun <T> ArgMap.ifArg(name: String, arg: ArgMap.(name: String) -> T) =
    if (containsKey(name)) {
        arg(name)
    } else {
        null
    }

fun List<KSValueArgument>.mapProperties(): ArgMap =
    associateBy { it.name!!.getShortName() }

inline fun <reified T : Enum<T>> ArgMap.enumArg(
    name: String,
    valueOf: (value: String) -> T
): T {
    return valueOf(arg<KSType>(name).declaration.qualifiedName!!.getShortName())
}