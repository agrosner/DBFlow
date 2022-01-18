package com.dbflow5.codegen.shared.properties

import com.dbflow5.annotation.ConflictAction

/**
 * Description:
 */
data class ReferenceProperties(
    override val name: String,
    val referencedName: String,
    val defaultValue: String,
    val onNullConflict: ConflictAction,
) : NamedProperties
