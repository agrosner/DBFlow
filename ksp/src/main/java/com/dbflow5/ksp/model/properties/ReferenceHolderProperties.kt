package com.dbflow5.ksp.model.properties

import com.dbflow5.annotation.ForeignKeyAction
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName

/**
 * Description:
 */
data class ReferenceHolderProperties(
    val onDelete: ForeignKeyAction,
    val onUpdate: ForeignKeyAction,
    val referencesType: ReferencesType,
    val referencedTableTypeName: TypeName,
) {

    sealed interface ReferencesType {
        object All : ReferencesType
        data class Specific(
            val references: List<ReferenceProperties>
        ) : ReferencesType
    }
}

/**
 * If true, use the field type.
 */
fun ReferenceHolderProperties.isInferredTable() =
    referencedTableTypeName != Any::class.asClassName()
