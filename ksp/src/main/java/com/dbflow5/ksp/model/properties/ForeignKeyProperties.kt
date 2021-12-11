package com.dbflow5.ksp.model.properties

import com.dbflow5.annotation.ForeignKeyAction

/**
 * Description:
 */
data class ForeignKeyProperties(
    val onDelete: ForeignKeyAction,
    val onUpdate: ForeignKeyAction,
    val referencesType: ReferencesType,
) {

    sealed interface ReferencesType {
        object All : ReferencesType
        data class Specific(
            val references: List<ReferenceProperties>
        ) : ReferencesType
    }
}
