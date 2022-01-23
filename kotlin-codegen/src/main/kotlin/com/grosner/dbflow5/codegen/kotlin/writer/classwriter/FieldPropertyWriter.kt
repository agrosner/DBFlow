package com.grosner.dbflow5.codegen.kotlin.writer.classwriter

import com.dbflow5.codegen.shared.ClassNames
import com.grosner.dbflow5.codegen.kotlin.kotlinpoet.MemberNames
import com.dbflow5.codegen.shared.writer.TypeCreator
import com.dbflow5.codegen.shared.ClassModel
import com.dbflow5.codegen.shared.FieldModel
import com.dbflow5.codegen.shared.TypeConverterModel
import com.dbflow5.codegen.shared.cache.TypeConverterCache
import com.dbflow5.codegen.shared.hasTypeConverter
import com.dbflow5.codegen.shared.typeConverter
import com.squareup.kotlinpoet.CodeBlock
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
            val nullableDataTypeName = typeConverterModel.dataTypeName
                .copy(nullable = model.classType.isNullable)
            return PropertySpec.builder(
                model.propertyName,
                ClassNames.typeConvertedProperty(
                    nullableDataTypeName,
                    model.classType,
                )
            )
                .addAnnotation(JvmField::class)
                .initializer(
                    CodeBlock.builder()
                        .apply {
                            add(
                                "%M(",
                                MemberNames.typeConvertedProperty,
                            )
                            if (!nullableDataTypeName.isNullable) {
                                add("%M(),", MemberNames.classToken)
                            }
                            // add non-null tokens to skirt platform clashes.
                            if (!model.classType.isNullable) {
                                add("%M(),", MemberNames.classToken)
                            }
                            add(
                                "%S) { ",
                                model.dbName,
                            )
                            when (typeConverterModel) {
                                is TypeConverterModel.Simple -> listOf(typeConverterModel)
                                is TypeConverterModel.Chained -> typeConverterModel.chainedConverters.toMutableList()
                                    .apply { add(0, typeConverterModel) }
                            }.reversed()
                                .forEachIndexed { index, model ->
                                    if (index > 0) {
                                        add(".%M(", MemberNames.chain)
                                    }
                                    add("%T()", model.classType)
                                    if (index > 0) {
                                        add(")")
                                    }
                                }

                            add("}")
                        }
                        .build()
                )
                .build()
        }
        return PropertySpec.builder(
            model.propertyName,
            ClassNames.property(model.classType)
        )
            .addAnnotation(JvmField::class)
            .initializer("%M(%S)", MemberNames.property, model.dbName)
            .build()
    }
}