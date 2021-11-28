package com.dbflow5.ksp.model.properties

import com.squareup.kotlinpoet.TypeName

/**
 * Description:
 */
interface NamedProperties {
    val name: String
}

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

