package com.dbflow5.ksp.writer.classwriter

import com.dbflow5.ksp.ClassNames
import com.dbflow5.ksp.MemberNames
import com.dbflow5.ksp.model.ClassModel
import com.dbflow5.ksp.model.FieldModel
import com.dbflow5.ksp.model.cache.TypeConverterCache
import com.dbflow5.ksp.model.hasTypeConverter
import com.dbflow5.ksp.model.typeConverter
import com.dbflow5.ksp.writer.TypeCreator
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.PropertySpec

/**
 * Description: Writes a property on the [ClassModel] companion.
 */
class FieldPropertyWriter(
    private val typeConverterCache: TypeConverterCache,
) : TypeCreator<FieldModel, PropertySpec> {

    override fun create(model: FieldModel): PropertySpec {

        if (model.hasTypeConverter(typeConverterCache)) {
            val typeConverterModel = model.typeConverter(typeConverterCache)
            return PropertySpec.builder(
                model.propertyName,
                ClassNames.typeConvertedProperty(
                    typeConverterModel.dataClassType,
                    model.classType,
                )
            )
                .addAnnotation(
                    AnnotationSpec.builder(JvmStatic::class)
                        .build()
                )
                .initializer(
                    "%M<%T, %T, %T>(%S) { %T() }",
                    MemberNames.typeConvertedProperty,
                    model.enclosingClassType,
                    typeConverterModel.dataClassType,
                    model.classType,
                    model.dbName,
                    typeConverterModel.classType,
                )
                .build()
        }
        return PropertySpec.builder(
            model.propertyName,
            ClassNames.property(model.classType)
        )
            .addAnnotation(
                AnnotationSpec.builder(JvmStatic::class)
                    .build()
            )
            .initializer("%M(%S)", MemberNames.property, model.dbName)
            .build()
    }
}