package com.dbflow5.ksp.model

import com.dbflow5.ksp.ClassNames
import com.dbflow5.ksp.model.cache.ReferencesCache
import com.dbflow5.ksp.model.cache.TypeConverterCache
import com.dbflow5.ksp.model.interop.KSPClassDeclaration
import com.dbflow5.ksp.writer.FieldExtractor
import com.dbflow5.model.ClassModel
import com.dbflow5.model.FieldModel
import com.dbflow5.model.NameModel
import com.dbflow5.model.ReferenceHolderModel
import com.dbflow5.model.SingleFieldModel
import com.dbflow5.model.TypeConverterModel
import com.dbflow5.model.properties.ReferenceHolderProperties
import com.dbflow5.model.properties.TypeConverterProperties
import com.dbflow5.model.properties.isInferredTable
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import java.util.*


fun FieldModel.hasTypeConverter(typeConverterCache: TypeConverterCache) =
    properties?.let { properties ->
        properties.typeConverterClassName as TypeName != ClassNames.TypeConverter
            || typeConverterCache.has(classType)
    }
        ?: typeConverterCache.has(classType)

fun FieldModel.typeConverter(typeConverterCache: TypeConverterCache) =
    typeConverterCache[classType, properties?.typeConverterClassName?.toString()
        ?: ""]

/**
 * Builds a new [TypeConverterModel] to generate on the fly.
 */
fun FieldModel.generateTypeConverter(): TypeConverterModel {
    val newName = name.copy(
        shortName = "${
            name.shortName.replaceFirstChar {
                if (it.isLowerCase())
                    it.titlecase(Locale.getDefault()) else it.toString()
            }
        }Converter"
    )
    val inlineDeclaration =
        (ksClassType.declaration.closestClassDeclaration as KSPClassDeclaration?)?.ksClassDeclaration!!
    val firstProperty = inlineDeclaration.getAllProperties().first()
    return TypeConverterModel.Simple(
        name = newName,
        properties = TypeConverterProperties(listOf()),
        classType = newName.className,
        dataTypeName = firstProperty.type.toTypeName(),
        modelTypeName = classType,
        modelClass = KSPClassDeclaration(inlineDeclaration),
        originatingFile = originatingFile,
    )
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
        originatingFile = originatingFile,
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

fun SingleFieldModel.toExtractor(classModel: ClassModel) = FieldExtractor.SingleFieldExtractor(
    this,
    classModel,
)

fun ReferenceHolderModel.toExtractor(
    classModel: ClassModel,
    referencesCache: ReferencesCache
) = FieldExtractor.ForeignFieldExtractor(
    this,
    referencesCache,
    classModel
)

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
                    is FieldModel.FieldType.PrimaryAuto -> referenceFieldType.copy(
                        isAutoIncrement = false,
                        isRowId = false,
                    )
                    FieldModel.FieldType.Normal -> referenceFieldType
                }
            }
        )
    } else reference
}
