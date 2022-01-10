package com.dbflow5.codegen.model.properties

import com.squareup.kotlinpoet.TypeName

/**
 * Description:
 */
data class TypeConverterProperties(
    val allowedSubtypeTypeNames: List<TypeName>,
)
