package com.dbflow5.ksp.writer.classwriter

import com.dbflow5.ksp.ClassNames
import com.dbflow5.model.ClassModel
import com.dbflow5.ksp.model.cache.ReferencesCache
import com.dbflow5.ksp.model.flattenedFields
import com.dbflow5.model.writer.TypeCreator
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.asClassName

/**
 * Description:
 */
class AllColumnPropertiesWriter(
    private val referencesCache: ReferencesCache,
): TypeCreator<ClassModel, PropertySpec> {

    override fun create(model: ClassModel): PropertySpec {
       return PropertySpec.builder(
            name = "allColumnProperties",
            type = Array::class.asClassName()
                .parameterizedBy(ClassNames.IProperty),
            KModifier.OVERRIDE,
        )
            .getter(
                FunSpec.getterBuilder()
                .addCode("return arrayOf(\n")
                .apply {
                    model.flattenedFields(referencesCache).forEach { field ->
                        addCode(
                            "%L.%L,\n",
                            "Companion",
                            field.propertyName,
                        )
                    }
                }
                .addCode(")")
                .build())
            .build()
    }
}