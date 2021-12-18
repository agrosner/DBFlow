package com.dbflow5.ksp.writer

import com.dbflow5.ksp.ClassNames
import com.dbflow5.ksp.MemberNames
import com.dbflow5.ksp.model.ClassModel
import com.dbflow5.ksp.model.FieldModel
import com.dbflow5.ksp.model.cache.TypeConverterCache
import com.dbflow5.ksp.model.hasTypeConverter
import com.dbflow5.ksp.model.typeConverter
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
            .initializer("%M(%S)", MemberNames.property, model.name.shortName)
            .build()
    }
}