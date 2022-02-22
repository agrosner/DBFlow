package com.dbflow5.codegen.shared

import com.dbflow5.codegen.shared.cache.ReferencesCache
import com.dbflow5.codegen.shared.interop.ClassType
import com.dbflow5.codegen.shared.interop.OriginatingSource
import com.dbflow5.codegen.shared.properties.ClassProperties
import com.dbflow5.codegen.shared.properties.GeneratedClassProperties
import com.dbflow5.codegen.shared.properties.ModelViewQueryProperties
import com.dbflow5.codegen.shared.properties.NamedProperties
import com.dbflow5.codegen.shared.properties.nameWithFallback
import com.dbflow5.quoteIfNeeded
import com.dbflow5.stripQuotes
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.NameAllocator
import com.squareup.kotlinpoet.TypeName

data class ClassModel(
    val name: NameModel,
    /**
     * Declared type of the class.
     */
    val classType: ClassName,
    val type: Type,
    val ksClassType: ClassType,
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
    /**
     * Views and Queries do not support granular since they relate to
     * table operations.
     */
    val granularNotifications: Boolean,
    val implementsLoadFromCursorListener: Boolean,
    val implementsDatabaseStatementListener: Boolean,
    override val originatingSource: OriginatingSource?,
) : ObjectModel, GeneratedClassModel {

    override val generatedClassName: NameModel = NameModel(
        packageName = name.packageName,
        shortName = "${name.shortName}_${
            when (type) {
                is Type.Table -> "Table"
                is Type.Query -> "Query"
                is Type.View -> "View"
            }
        }",
        nullable = false,
    )

    fun generatedAdapterName(nameAllocator: NameAllocator): NameModel {
        return NameModel(
            packageName = name.packageName,
            shortName = "${nameAllocator[generatedClassName]}_" + when (type) {
                is Type.Table -> "adapter"
                is Type.View -> "viewAdapter"
                is Type.Query -> "queryAdapter"
            },
            nullable = false,
        )
    }

    override val generatedSuperClass = when (type) {
        is Type.Table -> ClassNames.modelAdapter2(classType)
        is Type.View -> ClassNames.viewAdapter2(classType)
        Type.Query -> ClassNames.queryAdapter2(classType)
    }

    override val generatedFieldName
        get() = dbName.stripQuotes().replaceFirstChar { it.lowercase() }

    val primaryFields = fields.filter { it.fieldType is FieldModel.FieldType.Primary }
    val referenceFields = fields.filterIsInstance<ReferenceHolderModel>()
    val primaryAutoIncrementFields = primaryFields.filter {
        val fieldType = it.fieldType
        fieldType is FieldModel.FieldType.Primary
            && fieldType.isAutoIncrement
    }

    /**
     * Name to use on the database.
     */
    val dbName = when (properties) {
        is NamedProperties -> properties.nameWithFallback(name.shortName)
        else -> name.shortName
    }.quoteIfNeeded()

    val isQuery
        get() = type == Type.Query

    val isNormal
        get() = type is Type.Table

    val isView
        get() = type is Type.View

    fun flattenedFields(referencesCache: ReferencesCache) =
        createFlattenedFields(referencesCache, fields)

    fun primaryFlattenedFields(referencesCache: ReferencesCache) =
        createFlattenedFields(referencesCache, primaryFields)

    sealed interface Type {
        sealed interface Table : Type {
            object Fts3 : Table
            data class Fts4(
                val contentTable: TypeName,
            ) : Table

            object Normal : Table
        }

        data class View(
            val properties: ModelViewQueryProperties,
        ) : Type

        object Query : Type
    }
}

/**
 * Returns true if element exists in DB declaration, or if it self-declares its DB.
 */
inline fun <reified C : ClassModel.Type> ClassModel.partOfDatabaseAsType(
    databaseTypeName: TypeName,
    declaredDBElements: List<ClassName>,
    /**
     * Used for generated class.
     */
    allDBElements: List<ClassName>,
) = this.type is C &&
    (properties.database == databaseTypeName || declaredDBElements.contains(this.classType)
        || (properties is GeneratedClassProperties && allDBElements.contains(properties.generatedFromClassType)))


val ClassModel.memberSeparator
    get() = if (hasPrimaryConstructor) "," else ""

fun ClassModel.tableReferences(referencesCache: ReferencesCache) = referenceFields
    .filter { referencesCache.isTable(it) || it.type == ReferenceHolderModel.Type.Reference }

fun ClassModel.distinctAdapterGetters(referencesCache: ReferencesCache): List<GeneratedClassModel> =
    tableReferences(referencesCache)
        .map { referencesCache.resolve(it) }
        .distinctBy { it.generatedSuperClass }
