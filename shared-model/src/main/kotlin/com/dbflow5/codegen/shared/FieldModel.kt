package com.dbflow5.codegen.shared

import com.dbflow5.codegen.shared.cache.ReferencesCache
import com.dbflow5.codegen.shared.cache.TypeConverterCache
import com.dbflow5.codegen.shared.interop.ClassNameResolver
import com.dbflow5.codegen.shared.interop.ClassType
import com.dbflow5.codegen.shared.interop.OriginatingSource
import com.dbflow5.codegen.shared.properties.FieldProperties
import com.dbflow5.codegen.shared.properties.IndexProperties
import com.dbflow5.codegen.shared.properties.NotNullProperties
import com.dbflow5.codegen.shared.properties.ReferenceHolderProperties
import com.dbflow5.codegen.shared.properties.TypeConverterProperties
import com.dbflow5.codegen.shared.properties.UniqueProperties
import com.dbflow5.codegen.shared.properties.isInferredTable
import com.dbflow5.codegen.shared.properties.nameWithFallback
import com.squareup.kotlinpoet.TypeName
import java.util.*


sealed interface FieldModel : ObjectModel {
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

    val ksClassType: ClassType

    /**
     * If type is inline.
     */
    val isInlineClass: Boolean

    val isEnum: Boolean

    /**
     * If true, must exist in constructor, otherwise will be ignored.
     */
    val isVal: Boolean

    val fieldType: FieldType
    val properties: FieldProperties?
    val indexProperties: IndexProperties?
    val notNullProperties: NotNullProperties?
    val uniqueProperties: UniqueProperties?

    /**
     * This can be View, Normal, or Query. Based on [ClassModel]
     */
    val enclosingClassType: TypeName

    /**
     *  Join by name for properties.
     */
    val propertyName
        get() = properties.nameWithFallback(names.joinToString("_") { it.shortName })

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
        data class Primary(
            val isAutoIncrement: Boolean,
            val isRowId: Boolean,
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
    override val enclosingClassType: TypeName,
    override val names: List<NameModel> = listOf(name),
    override val isInlineClass: Boolean,
    override val isVal: Boolean,
    override val isEnum: Boolean,
    override val ksClassType: ClassType,
    override val originatingSource: OriginatingSource?,
    override val indexProperties: IndexProperties?,
    override val notNullProperties: NotNullProperties?,
    override val uniqueProperties: UniqueProperties?,
) : ObjectModel, FieldModel

data class ReferenceHolderModel(
    override val name: NameModel,
    override val classType: TypeName,
    override val fieldType: FieldModel.FieldType,
    override val properties: FieldProperties?,
    val referenceHolderProperties: ReferenceHolderProperties,
    override val enclosingClassType: TypeName,
    override val names: List<NameModel> = listOf(name),
    val type: Type,
    override val isInlineClass: Boolean,
    override val ksClassType: ClassType,
    override val isVal: Boolean,
    val isColumnMap: Boolean,
    override val isEnum: Boolean,
    override val originatingSource: OriginatingSource?,
    /**
     * Indexes on Reference models will apply to all reference fields.
     */
    override val indexProperties: IndexProperties?,
    override val notNullProperties: NotNullProperties?,
    override val uniqueProperties: UniqueProperties?,
) : ObjectModel, FieldModel {

    enum class Type {
        ForeignKey,

        /**
         * These are either ColumnMap or inline classes.
         */
        Computed,

        /**
         * These are specifically used in [OneToManyReference] definitions.
         */
        Reference,
    }

}

fun ReferenceHolderModel.toSingleModel() =
    SingleFieldModel(
        name = name,
        classType = classType,
        fieldType = fieldType,
        properties = properties,
        enclosingClassType = enclosingClassType,
        names = names,
        isInlineClass = isInlineClass,
        isVal = isVal,
        isEnum = isEnum,
        ksClassType = ksClassType,
        originatingSource = originatingSource,
        indexProperties = indexProperties,
        notNullProperties = notNullProperties,
        uniqueProperties = uniqueProperties,
    )

fun createFlattenedFields(
    referencesCache: ReferencesCache,
    fields: List<FieldModel>
): List<FieldModel> {
    return fields.map { field ->
        when (field) {
            is ReferenceHolderModel -> field.references(
                referencesCache,
                nameToNest = field.name,
            )
            is SingleFieldModel -> listOf(field)
        }
    }.flatten()
}

fun ReferenceHolderModel.references(
    referencesCache: ReferencesCache,
    nameToNest: NameModel? = null,
): List<SingleFieldModel> {
    when (type) {
        ReferenceHolderModel.Type.Reference -> {
            // we currently only create these virtually (i.e. no public API)
            // so we can safely assume they will be of list type.
            val tableTypeName = referenceHolderProperties.referencedTableTypeName

            // for now only grab all references.
            return referencesCache.resolveOneToManyReferences(
                this,
                tableTypeName
            ).map(nestNameReference(nameToNest))
        }
        ReferenceHolderModel.Type.ForeignKey -> {
            // treat field of not table type as a single model type.
            if (!referencesCache.isTable(this)) {
                return listOf(toSingleModel())
            }
            val tableTypeName = if (referenceHolderProperties.isInferredTable()) {
                referenceHolderProperties.referencedTableTypeName
            } else {
                classType
            }
            return referenceHolderProperties.referencesType.let { type ->
                when (type) {
                    is ReferenceHolderProperties.ReferencesType.All -> referencesCache.resolveExistingFields(
                        this,
                        tableTypeName
                    )
                    is ReferenceHolderProperties.ReferencesType.Specific -> {
                        referencesCache.resolveReferencesOnExisting(
                            this,
                            type.references,
                            tableTypeName,
                        )
                    }
                }.map(nestNameReference(nameToNest))
            }
        }
        ReferenceHolderModel.Type.Computed -> {
            return referenceHolderProperties.referencesType.let { type ->
                when (type) {
                    is ReferenceHolderProperties.ReferencesType.All -> referencesCache.resolveComputedFields(
                        this,
                        ksClassType,
                    )
                    is ReferenceHolderProperties.ReferencesType.Specific -> {
                        referencesCache.resolveReferencesOnComputedFields(
                            this,
                            type.references,
                            ksClassType,
                        )
                    }
                }
            }
        }
    }
}

private fun nestNameReference(nameToNest: NameModel?) = { reference: SingleFieldModel ->
    if (nameToNest != null) {
        reference.copy(
            names = reference.names.toMutableList().apply {
                add(0, nameToNest)
            },
            // when referencing fields, we don't need to know it is autoincrement
            // or rowId and assumes the reference is primaryauto.
            fieldType = reference.fieldType.let { referenceFieldType ->
                when (referenceFieldType) {
                    is FieldModel.FieldType.Primary -> referenceFieldType.copy(
                        isAutoIncrement = false,
                        isRowId = false,
                    )
                    FieldModel.FieldType.Normal -> referenceFieldType
                }
            }
        )
    } else reference
}

fun FieldModel.hasTypeConverter(typeConverterCache: TypeConverterCache): Boolean {
    return properties?.let { properties ->
        properties.typeConverterClassName as TypeName != ClassNames.TypeConverter
            || typeConverterCache.has(classType)
    }
        ?: typeConverterCache.has(classType)
}

fun FieldModel.typeConverter(typeConverterCache: TypeConverterCache) =
    typeConverterCache[classType, properties?.typeConverterClassName?.toString()
        ?: ""]


/**
 * Builds a new [TypeConverterModel] to generate on the fly.
 */
fun FieldModel.generateTypeConverter(resolver: ClassNameResolver): TypeConverterModel {
    val newName = name.copy(
        shortName = "${
            name.shortName.replaceFirstChar {
                if (it.isLowerCase())
                    it.titlecase(Locale.getDefault()) else it.toString()
            }
        }Converter"
    )
    val classDeclaration = ksClassType.declaration.closestClassDeclaration
    val properties = classDeclaration!!.properties
    val firstProperty = properties.first()
    return TypeConverterModel.Simple(
        name = newName,
        properties = TypeConverterProperties(listOf()),
        classType = newName.className,
        dataTypeName = firstProperty.typeName,
        modelTypeName = classType,
        modelClass = classDeclaration,
        originatingSource = originatingSource,
    )
}

/**
 * Will keep nullability from referenced type.
 */
fun SingleFieldModel.preserveNullabilityFrom(
    other: FieldModel
) = copy(
    // preserve nullability
    classType = classType.copy(
        nullable = other.classType.isNullable,
    ),
    name = name.copy(
        nullable = other.name.nullable,
    )
)