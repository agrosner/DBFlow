package com.dbflow5.ksp.writer

import com.dbflow5.ksp.ClassNames
import com.dbflow5.ksp.MemberNames
import com.dbflow5.ksp.model.FieldModel
import com.dbflow5.ksp.model.cache.TypeConverterCache
import com.dbflow5.ksp.model.hasTypeConverter
import com.dbflow5.ksp.model.typeConverter
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import java.util.*

/**
 * Description:
 */
class PropertyStatementWrapperWriter(
    private val typeConverterCache: TypeConverterCache,
) : TypeCreator<FieldModel, PropertySpec> {

    override fun create(model: FieldModel): PropertySpec {
        val hasTypeConverter = model.hasTypeConverter(typeConverterCache)
        val type =
            when {
                hasTypeConverter -> {
                    ClassNames.typeConvertedPropertyStatementWrapper(
                        model.classType,
                        model.typeConverter(typeConverterCache).dataClassType,
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
                            add(
                                "(%L)", model.typeConverter(typeConverterCache)
                                    .name.shortName.lowercase(Locale.getDefault())
                            )
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