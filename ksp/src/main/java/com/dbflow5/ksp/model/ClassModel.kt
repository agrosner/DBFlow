package com.dbflow5.ksp.model

import com.dbflow5.ksp.model.properties.ClassProperties
import com.dbflow5.ksp.model.properties.NamedProperties
import com.dbflow5.ksp.model.properties.nameWithFallback
import com.dbflow5.quoteIfNeeded
import com.squareup.kotlinpoet.ClassName

data class ClassModel(
    val name: NameModel,
    /**
     * Declared type of the class.
     */
    val classType: ClassName,
    val type: ClassType,
    val properties: ClassProperties,
    val fields: List<FieldModel>,
) : ObjectModel {

    val primaryFields = fields.filter { it.fieldType is FieldModel.FieldType.PrimaryAuto }

    /**
     * Name to use on the database.
     */
    val dbName = when (properties) {
        is NamedProperties -> properties.nameWithFallback(name.shortName)
        else -> name.shortName
    }.quoteIfNeeded()

    private fun createFlattenedFields(
        referencesCache: ReferencesCache,
        primaryFields: List<FieldModel>
    ): List<FieldModel> {
        return primaryFields.map { field ->
            when (field) {
                is ForeignKeyModel -> field.references(
                    referencesCache,
                    namePrefix = field.dbName
                )
                is SingleFieldModel -> listOf(field)
            }
        }.flatten()
    }

    fun flattenedFields(referencesCache: ReferencesCache) =
        createFlattenedFields(referencesCache, fields)

    fun primaryFlattenedFields(referencesCache: ReferencesCache) =
        createFlattenedFields(referencesCache, primaryFields)

    sealed interface ClassType {
        object Normal : ClassType
        object View : ClassType
        object Query : ClassType
    }
}
