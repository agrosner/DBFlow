package com.dbflow5.ksp.kotlinpoet

import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.Nullability
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName

/**
 * Marks all platform types as nullable.
 */
fun KSTypeReference.javaPlatformTypeName(): TypeName {
    val type = resolve()
    if (type.nullability == Nullability.PLATFORM) {
        return type.toTypeName()
            .copy(nullable = true)
    }
    return type.toTypeName()
}