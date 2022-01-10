package com.dbflow5.model.properties

import com.squareup.kotlinpoet.TypeName

/**
 * Description:
 */
data class TypeConverterProperties(
    val allowedSubtypeTypeNames: List<TypeName>,
)
