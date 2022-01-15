package com.dbflow5.ksp.model

import com.dbflow5.codegen.model.ClassModel
import com.dbflow5.codegen.model.FieldModel
import com.dbflow5.codegen.model.ReferenceHolderModel
import com.dbflow5.codegen.model.SingleFieldModel
import com.dbflow5.codegen.model.TypeConverterModel
import com.dbflow5.codegen.model.cache.ReferencesCache
import com.dbflow5.codegen.model.properties.TypeConverterProperties
import com.dbflow5.ksp.model.interop.KSPClassDeclaration
import com.dbflow5.ksp.writer.FieldExtractor
import com.squareup.kotlinpoet.ksp.toTypeName
import java.util.Locale

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
