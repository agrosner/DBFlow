package com.dbflow5.ksp.model

import com.dbflow5.ksp.ClassNames
import com.dbflow5.ksp.model.cache.ReferencesCache
import com.dbflow5.ksp.model.cache.TypeConverterCache
import com.dbflow5.ksp.model.properties.FieldProperties
import com.dbflow5.ksp.model.properties.ForeignKeyProperties
import com.dbflow5.ksp.model.properties.isInferredTable
import com.dbflow5.ksp.model.properties.nameWithFallback
import com.squareup.kotlinpoet.TypeName


sealed interface FieldModel {
    /**
     * The original name.
     */
    val name: NameModel

    /**
     * List of names, nested by call
     */
    val names: List<NameModel>

    /**
     * The declared type of the field.
     */
    val classType: TypeName
    val nonNullClassType: TypeName
        get() = classType.copy(nullable = false)
    val fieldType: FieldType
    val properties: FieldProperties?

    /**
     * This can be View, Normal, or Query. Based on [ClassModel]
     */
    val enclosingClassType: TypeName

    /**
     *  Join by name for properties.
     */
    val propertyName
        get() = names.joinToString("_") { it.shortName }

    /**
     * [useLastNull] Last name if we want ? inserted
     */
    fun accessName(useLastNull: Boolean = false) = names
        .withIndex()
        .joinToString(".") { (index, value) ->
            if (index < names.size - 1 || useLastNull) {
                value.accessName
            } else {
                value.shortName
            }
        }

    val dbName
        get() = properties.nameWithFallback(propertyName)

    sealed interface FieldType {
        object Normal : FieldType
        data class PrimaryAuto(
            val isAutoIncrement: Boolean,
            val isRowId: Boolean,
            val quickCheckPrimaryKey: Boolean,
        ) : FieldType
    }

}

fun FieldModel.hasTypeConverter(typeConverterCache: TypeConverterCache) =
    properties?.let { properties ->
        properties.typeConverterClassName as TypeName != ClassNames.TypeConverter
    }
        ?: typeConverterCache.has(classType)

fun FieldModel.typeConverter(typeConverterCache: TypeConverterCache) =
    typeConverterCache[classType, properties?.typeConverterClassName?.toString()
        ?: ""]

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
    override val enclosingClassType: TypeName,
    override val names: List<NameModel> = listOf(name),
) : ObjectModel, FieldModel

data class ForeignKeyModel(
    override val name: NameModel,
    override val classType: TypeName,
    override val fieldType: FieldModel.FieldType,
    override val properties: FieldProperties?,
    val foreignKeyProperties: ForeignKeyProperties,
    override val enclosingClassType: TypeName,
    override val names: List<NameModel> = listOf(name),
) : ObjectModel, FieldModel {

    fun references(
        referencesCache: ReferencesCache,
        nameToNest: NameModel? = null,
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
            if (nameToNest != null) {
                reference.copy(
                    names = reference.names.toMutableList().apply {
                        add(0, nameToNest)
                    }
                )
            } else reference
        }
    }
}


