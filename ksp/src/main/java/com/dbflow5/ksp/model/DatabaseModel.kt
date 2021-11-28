package com.dbflow5.ksp.model

import com.dbflow5.annotation.ConflictAction
import com.google.devtools.ksp.symbol.KSName
import com.squareup.kotlinpoet.TypeName

/**
 * Description:
 */
data class DatabaseModel(
    val name: KSName,
    val classType: TypeName,
    val properties: Properties,
) : ObjectModel {

    data class Properties(
        val version: Int,
        val foreignKeyConstraintsEnforced: Boolean,
        val insertConflict: ConflictAction,
        val updateConflict: ConflictAction,
    )
}