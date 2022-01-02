package com.dbflow5.ksp.model

import com.dbflow5.ksp.model.cache.ReferencesCache
import com.dbflow5.ksp.model.properties.ClassProperties
import com.dbflow5.ksp.model.properties.ModelViewQueryProperties
import com.dbflow5.ksp.model.properties.NamedProperties
import com.dbflow5.ksp.model.properties.nameWithFallback
import com.dbflow5.quoteIfNeeded
import com.google.devtools.ksp.symbol.KSFile
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
    val indexGroups: List<IndexGroupModel>,
    val uniqueGroups: List<UniqueGroupModel>,
    /**
     * If true we use that, other wise expect all mutable fields
     * (to remain compatible with old DBFlow models).
     */
    val hasPrimaryConstructor: Boolean,
    /**
     * If true, generated adapter will also generate internal.
     */
    val isInternal: Boolean,
    override val originatingFile: KSFile?,
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
        get() = type is ClassType.Normal

    fun flattenedFields(referencesCache: ReferencesCache) =
        createFlattenedFields(referencesCache, fields)

    fun primaryFlattenedFields(referencesCache: ReferencesCache) =
        createFlattenedFields(referencesCache, primaryFields)

    sealed interface ClassType {
        sealed interface Normal : ClassType {
            object Fts3 : ClassType.Normal
            data class Fts4(
                val contentTable: TypeName,
            ) : ClassType.Normal

            object Normal : ClassType.Normal
        }

        data class View(
            val properties: ModelViewQueryProperties,
        ) : ClassType

        object Query : ClassType
    }
}

/**
 * Returns true if element exists in DB declaration, or if it self-declares its DB.
 */
inline fun <reified C : ClassModel.ClassType> ClassModel.partOfDatabaseAsType(
    databaseTypeName: TypeName,
    declaredDBElements: List<ClassName>,
) = this.type is C &&
    (properties.database == databaseTypeName || declaredDBElements.contains(this.classType))


val ClassModel.generatedClassName
    get() = NameModel(
        packageName = name.packageName,
        shortName = "${name.shortName}_${
            when (type) {
                is ClassModel.ClassType.Normal -> "Table"
                is ClassModel.ClassType.Query -> "Query"
                is ClassModel.ClassType.View -> "View"
            }
        }"
    )

val ClassModel.memberSeparator
    get() = if (hasPrimaryConstructor) "," else ""