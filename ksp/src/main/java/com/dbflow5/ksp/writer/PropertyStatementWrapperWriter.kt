package com.dbflow5.ksp.writer

import com.dbflow5.ksp.ClassNames
import com.dbflow5.ksp.MemberNames
import com.dbflow5.ksp.model.FieldModel
import com.dbflow5.ksp.model.cache.TypeConverterCache
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName

/**
 * Description:
 */
class PropertyStatementWrapperWriter(
    private val typeConverterCache: TypeConverterCache,
) : TypeCreator<FieldModel, PropertySpec> {

    override fun create(model: FieldModel): PropertySpec {
        val hasTypeConverter =
            model.properties?.let { properties ->
                properties.typeConverterClassName as TypeName != ClassNames.TypeConverter
            }
                ?: false
        val type =
            when {
                hasTypeConverter -> {
                    ClassNames.typeConvertedPropertyStatementWrapper(
                        model.classType,
                        typeConverterCache[model.classType, model.properties?.typeConverterClassName?.toString()
                            ?: ""].dataClassType,
                    )
                }
                model.classType.isNullable -> {
                    ClassNames.nullablePropertyStatementWrapper(model.classType)
                }
                else -> {
                    ClassNames.propertyStatementWrapper(model.classType)
                }
            }
        return PropertySpec.builder(
            model.fieldWrapperName,
            type,
            KModifier.PRIVATE,
        )
            .apply {
                initializer(
                    CodeBlock.builder().apply {
                        add("%T", type)
                        if (hasTypeConverter) {
                            add("(typeConverter)")
                        }
                        add(
                            """
                        { data, statement, index -> statement.%M(index, data) }
                        """.trimIndent(),
                            MemberNames.bind,
                        )
                    }.build()
                )
            }
            .build()
    }
}


val FieldModel.fieldWrapperName get() = "${name.shortName}_wrapper"