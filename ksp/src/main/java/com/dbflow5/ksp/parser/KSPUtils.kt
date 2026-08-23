package com.dbflow5.ksp.parser

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.typeNameOf

inline fun <reified T> KSPropertyDeclaration.hasAnnotation() = annotations.anyAnnotation<T>()
    || getter?.annotations?.anyAnnotation<T>() ?: false
    || setter?.annotations?.anyAnnotation<T>() ?: false

inline fun <reified T> KSFunctionDeclaration.hasAnnotation() = annotations.anyAnnotation<T>()
inline fun <reified T> KSClassDeclaration.hasAnnotation() = annotations.anyAnnotation<T>()
inline fun <reified T> Sequence<KSAnnotation>.anyAnnotation() = any { a ->
    a.annotationType.toTypeName() == typeNameOf<T>()
}

fun KSClassDeclaration.hasSuperType(
    superTypeName: TypeName
) = superTypes.any { it.toTypeName() == superTypeName }
