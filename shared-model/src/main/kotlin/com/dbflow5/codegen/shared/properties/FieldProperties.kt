package com.dbflow5.codegen.shared.properties

import com.dbflow5.annotation.Collate
import com.squareup.kotlinpoet.ClassName

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
    val typeConverterClassName: ClassName,
) : NamedProperties
