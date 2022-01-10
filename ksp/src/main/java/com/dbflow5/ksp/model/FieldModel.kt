package com.dbflow5.ksp.model

import com.dbflow5.ksp.ClassNames
import com.dbflow5.codegen.model.cache.ReferencesCache
import com.dbflow5.ksp.model.cache.TypeConverterCache
import com.dbflow5.ksp.model.interop.KSPClassDeclaration
import com.dbflow5.ksp.writer.FieldExtractor
import com.dbflow5.codegen.model.ClassModel
import com.dbflow5.codegen.model.FieldModel
import com.dbflow5.codegen.model.NameModel
import com.dbflow5.codegen.model.ReferenceHolderModel
import com.dbflow5.codegen.model.SingleFieldModel
import com.dbflow5.codegen.model.TypeConverterModel
import com.dbflow5.codegen.model.properties.ReferenceHolderProperties
import com.dbflow5.codegen.model.properties.TypeConverterProperties
import com.dbflow5.codegen.model.properties.isInferredTable
import com.dbflow5.codegen.model.references
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
