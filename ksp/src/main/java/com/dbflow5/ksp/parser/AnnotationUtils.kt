package com.dbflow5.ksp.parser

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.typeNameOf

/**
 * Finds a single annotation instance on a [KSAnnotated] type.
 */
inline fun <reified T> KSAnnotated.findSingle(): KSAnnotation? =
    annotations.find { it.annotationType.toTypeName() == typeNameOf<T>() }
