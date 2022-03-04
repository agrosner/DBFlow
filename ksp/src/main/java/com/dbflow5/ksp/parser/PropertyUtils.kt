package com.dbflow5.ksp.parser

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueArgument
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName

typealias ArgMap = Map<String, KSValueArgument>

inline fun <reified T> ArgMap.arg(name: String): T? {
    return getValue(name).value as T?
}

inline fun <reified T> ArgMap.expectedArg(name: String): T {
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
    defValue: T,
    valueOf: (value: String) -> T
): T {
    return arg<KSType?>(name)?.let { arg -> valueOf(arg.declaration.qualifiedName!!.getShortName()) }
        ?: defValue
}

fun ArgMap.typeName(name: String): TypeName? =
    arg<KSType?>(name)?.toTypeName()

fun ArgMap.className(name: String) =
    arg<KSType?>(name)?.toClassName()

fun ArgMap.annotationMap(name: String) =
    arg<KSAnnotation>(name)?.arguments?.mapProperties() ?: mutableMapOf()

fun ArgMap.classNameList(name: String) =
    arg<List<KSType>?>(name)?.map { it.toClassName() } ?: listOf()
