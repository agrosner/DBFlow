package com.dbflow5.model.properties

import com.squareup.kotlinpoet.TypeName

/**
 * Description:
 */
interface NamedProperties {
    val name: String

}

fun NamedProperties?.nameWithFallback(fallback: String) =
    this?.name.nameWithFallback(fallback)

fun String?.nameWithFallback(fallback: String) =
    (this?.takeIf { it.isNotBlank() }
        ?: fallback)

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

