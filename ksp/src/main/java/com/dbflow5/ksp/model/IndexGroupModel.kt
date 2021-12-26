package com.dbflow5.ksp.model

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