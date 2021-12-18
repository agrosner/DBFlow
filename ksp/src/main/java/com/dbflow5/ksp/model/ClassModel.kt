package com.dbflow5.ksp.model

import com.dbflow5.ksp.model.cache.ReferencesCache
import com.dbflow5.ksp.model.properties.ClassProperties
import com.dbflow5.ksp.model.properties.NamedProperties
import com.dbflow5.ksp.model.properties.nameWithFallback
import com.dbflow5.quoteIfNeeded
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

data class ClassModel(
    val name: NameModel,
    /**
     * Declared type of the class.
     */
    val classType: ClassName,
    val type: ClassType,
    val properties: ClassProperties,
    val fields: List<FieldModel>,
    /**
     * If true we use that, other wise expect all mutable fields
     * (to remain compatible with old DBFlow models).
     */
    val hasPrimaryConstructor: Boolean,
) : ObjectModel {

    val primaryFields = fields.filter { it.fieldType is FieldModel.FieldType.PrimaryAuto }

    /**
     * Name to use on the database.
     */
    val dbName = when (properties) {
        is NamedProperties -> properties.nameWithFallback(name.shortName)
        else -> name.shortName
    }.quoteIfNeeded()

    val isQuery
        get() = type == ClassType.Query

    val isNormal
        get() = type == ClassType.Normal

    private fun createFlattenedFields(
        referencesCache: ReferencesCache,
        primaryFields: List<FieldModel>
    ): List<FieldModel> {
        return primaryFields.map { field ->
            when (field) {
                is ForeignKeyModel -> field.references(
                    referencesCache,
                    nameToNest = field.name,
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

fun ClassModel.partOfDatabaseAsType(
    databaseTypeName: TypeName,
    type: ClassModel.ClassType,
) = type == this.type &&
    properties.database == databaseTypeName


val ClassModel.generatedClassName
    get() = NameModel(
        packageName = name.packageName,
        shortName = "${name.shortName}_${
            when (type) {
                is ClassModel.ClassType.Normal -> "Adapter"
                is ClassModel.ClassType.Query -> "Query"
                is ClassModel.ClassType.View -> "View"
            }
        }"
    )

val ClassModel.memberSeparator
    get() = if (hasPrimaryConstructor) "," else ""