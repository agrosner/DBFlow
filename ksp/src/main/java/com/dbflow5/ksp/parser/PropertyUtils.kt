package com.dbflow5.ksp.parser

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueArgument
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.typeNameOf

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

fun ArgMap.typeName(name: String) =
    arg<KSType>(name).toTypeName()

fun ArgMap.className(name: String) =
    arg<KSType>(name).toClassName()

inline fun <reified T> Sequence<KSAnnotation>.anyAnnotation() = any { a ->
    a.annotationType.toTypeName() == typeNameOf<T>()
}

inline fun <reified T> KSPropertyDeclaration.hasAnnotation() = annotations.anyAnnotation<T>()
    || getter?.annotations?.anyAnnotation<T>() ?: false
    || setter?.annotations?.anyAnnotation<T>() ?: false

inline fun <reified T> KSFunctionDeclaration.hasAnnotation() = annotations.anyAnnotation<T>()
