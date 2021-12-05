package com.dbflow5.ksp.model

import com.dbflow5.ksp.model.properties.FieldProperties
import com.dbflow5.quoteIfNeeded
import com.google.devtools.ksp.symbol.KSName
import com.squareup.kotlinpoet.TypeName

/**
 * Description:
 */
data class FieldModel(
    val name: KSName,

    /**
     * The declared type of the field.
     */
    val classType: TypeName,
    val fieldType: FieldType,
    val properties: FieldProperties?,
) : ObjectModel {

    val dbName = (properties?.nameWithFallback(
        name.getShortName()
    ) ?: name.getShortName())
        .quoteIfNeeded()

    sealed interface FieldType {
        object Normal : FieldType
        data class PrimaryAuto(
            val isAutoIncrement: Boolean,
            val isRowId: Boolean,
            val quickCheckPrimaryKey: Boolean,
        ) : FieldType
    }
}