package com.dbflow5.ksp.model

import com.dbflow5.ksp.model.cache.ReferencesCache
import com.dbflow5.ksp.model.properties.FieldProperties
import com.dbflow5.ksp.model.properties.ForeignKeyProperties
import com.dbflow5.ksp.model.properties.isInferredTable
import com.dbflow5.ksp.model.properties.nameWithFallback
import com.dbflow5.stripQuotes
import com.squareup.kotlinpoet.TypeName


sealed interface FieldModel {
    val name: NameModel

    /**
     * If this is a reference type, this field will represent original name of the
     * field within a specific model.
     */
    val originatingName: NameModel

    /**
     * The declared type of the field.
     */
    val classType: TypeName
    val nonNullClassType: TypeName
        get() = classType.copy(nullable = false)
    val fieldType: FieldType
    val properties: FieldProperties?

    val dbName
        get() = properties.nameWithFallback(name.shortName)

    sealed interface FieldType {
        object Normal : FieldType
        data class PrimaryAuto(
            val isAutoIncrement: Boolean,
            val isRowId: Boolean,
            val quickCheckPrimaryKey: Boolean,
        ) : FieldType
    }

}

/**
 * Description:
 */
data class SingleFieldModel(
    override val name: NameModel,

    /**
     * The declared type of the field.
     */
    override val classType: TypeName,
    override val fieldType: FieldModel.FieldType,
    override val properties: FieldProperties?,
    override val originatingName: NameModel = name,
) : ObjectModel, FieldModel

data class ForeignKeyModel(
    override val name: NameModel,
    override val classType: TypeName,
    override val fieldType: FieldModel.FieldType,
    override val properties: FieldProperties?,
    val foreignKeyProperties: ForeignKeyProperties,
    override val originatingName: NameModel = name,
) : ObjectModel, FieldModel {

    fun references(
        referencesCache: ReferencesCache,
        namePrefix: String
    ): List<SingleFieldModel> {
        val tableTypeName = if (foreignKeyProperties.isInferredTable()) {
            foreignKeyProperties.referencedTableTypeName
        } else {
            classType
        }
        return when (foreignKeyProperties.referencesType) {
            is ForeignKeyProperties.ReferencesType.All -> referencesCache[tableTypeName]
            is ForeignKeyProperties.ReferencesType.Specific -> {
                referencesCache.references(
                    foreignKeyProperties.referencesType.references,
                    tableTypeName,
                )
            }
        }.map { reference ->
            if (namePrefix.isNotBlank()) {
                reference.copy(
                    originatingName = reference.name,
                    name = reference.name.copy(
                        shortName = "${namePrefix.stripQuotes()}_${reference.name.shortName}"
                    )
                )
            } else reference
        }
    }
}


