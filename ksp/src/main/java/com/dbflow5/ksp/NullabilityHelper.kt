package com.dbflow5.ksp

import com.dbflow5.codegen.shared.shouldBeNonNull
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.Nullability
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName

/**
 * Marks all platform types as nullable if they do not have nullability annotations.
 */
fun KSTypeReference.nullablePlatformType(): TypeName {
    val type = resolve()
    if (type.nullability == Nullability.PLATFORM) {
        val shouldBeNonNull = type.annotations.any {
            it.annotationType.toTypeName()
                .shouldBeNonNull()
        }
        return type.toTypeName()
            .copy(nullable = !shouldBeNonNull)
    }
    return type.toTypeName()
}
