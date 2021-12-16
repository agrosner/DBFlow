package com.dbflow5.ksp.model.properties

import com.dbflow5.annotation.Collate
import com.squareup.kotlinpoet.TypeName

/**
 * Description:
 */
data class FieldProperties(
    override val name: String,
    val length: Int,
    val collate: Collate,
    val defaultValue: String,
    /**
     * If has type converter, use this field.
     */
    val typeConverterTypeName: TypeName,
) : NamedProperties
