package com.dbflow5.ksp.model.properties

import com.dbflow5.annotation.Collate

/**
 * Description:
 */
data class FieldProperties(
    override val name: String,
    val length: Int,
    val collate: Collate,
    val defaultValue: String,
) : NamedProperties
