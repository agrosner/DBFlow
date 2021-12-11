package com.dbflow5.ksp.model.properties

import com.dbflow5.quoteIfNeeded
import com.squareup.kotlinpoet.TypeName

/**
 * Description:
 */
interface NamedProperties {
    val name: String

}

fun NamedProperties?.nameWithFallback(fallback: String) =
    (this?.name.takeIf { it?.isNotBlank() == true }
        ?: fallback).quoteIfNeeded()

interface DatabaseScopedProperties {
    val database: TypeName
}

interface ReadableScopeProperties {
    val orderedCursorLookup: Boolean
    val assignDefaultValuesFromCursor: Boolean
}

interface CreatableScopeProperties {
    val createWithDatabase: Boolean
}

