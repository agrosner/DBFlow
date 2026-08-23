package com.dbflow5.ksp

import com.dbflow5.codegen.shared.shouldBeNonNull
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Nullability
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName

/**
 * Marks all platform types as nullable if they do not have nullability annotations.
 */
fun KSPropertyDeclaration.nullablePlatformType(): TypeName {
    val type = type.resolve()
    if (type.nullability == Nullability.PLATFORM) {
        val predicate: (KSAnnotation) -> Boolean = {
            val toTypeName = it.annotationType.toTypeName()
            toTypeName
                .shouldBeNonNull()
        }
        val shouldBeNonNull = annotations.any(predicate).takeIf { it }
            ?: getter?.annotations?.any(predicate) ?: false
        return type.toTypeName()
            .copy(nullable = !shouldBeNonNull)
    }
    return type.toTypeName()
}
