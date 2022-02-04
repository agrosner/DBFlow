package com.dbflow5.codegen.shared

import com.squareup.kotlinpoet.ClassName

/**
 * Description: Holds information for an index grouping.
 */
data class IndexGroupModel(
    val name: String,
    val fields: List<FieldModel>,
    val unique: Boolean,
    val tableTypeName: ClassName,
)
