package com.grosner.dbflow5.codegen.kotlin.writer.classwriter

import com.dbflow5.codegen.shared.ClassModel
import com.dbflow5.codegen.shared.ClassNames
import com.dbflow5.codegen.shared.FieldModel
import com.dbflow5.codegen.shared.TypeConverterModel
import com.dbflow5.codegen.shared.cache.TypeConverterCache
import com.dbflow5.codegen.shared.hasTypeConverter
import com.dbflow5.codegen.shared.typeConverter
import com.dbflow5.codegen.shared.writer.TypeCreator
import com.grosner.dbflow5.codegen.kotlin.kotlinpoet.MemberNames
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec

/**
 * Writes a column property as an extension on [com.dbflow5.adapter.AdapterCompanion].
 */
class FieldPropertyWriter(
    private val typeConverterCache: TypeConverterCache,
) : TypeCreator<Pair<ClassModel, FieldModel>, PropertySpec> {

    override fun create(model: Pair<ClassModel, FieldModel>): PropertySpec {
        val (classModel, field) = model
        if (field.hasTypeConverter(typeConverterCache)) {
            val typeConverterModel = field.typeConverter(typeConverterCache)
            val nullableDataTypeName = typeConverterModel.dataTypeName
                .copy(nullable = field.classType.isNullable)
            return PropertySpec.builder(
                field.propertyName,
                ClassNames.typeConvertedProperty(
                    nullableDataTypeName,
                    field.classType,
                    classModel.classType,
                )
            )
                .getter(
                    FunSpec.getterBuilder()
                        .addCode("return ")
                        .addCode(typeConvertedInitializer(field, typeConverterModel, nullableDataTypeName))
                        .build()
                )
                .build()
        }
        return PropertySpec.builder(
            field.propertyName,
            ClassNames.propertyStart(field.classType, classModel.classType)
        )
            .getter(
                FunSpec.getterBuilder()
                    .addStatement(
                        "return %M<%T, %T>(%S)",
                        MemberNames.property,
                        field.classType,
                        classModel.classType,
                        field.dbName
                    )
                    .build()
            )
            .build()
    }

    private fun typeConvertedInitializer(
        model: FieldModel,
        typeConverterModel: TypeConverterModel,
        nullableDataTypeName: com.squareup.kotlinpoet.TypeName,
    ): CodeBlock = CodeBlock.builder()
        .apply {
            add(
                "%M(",
                MemberNames.typeConvertedProperty,
            )
            if (!nullableDataTypeName.isNullable) {
                add("%M(),", MemberNames.classToken)
            }
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
                .forEachIndexed { index, converter ->
                    if (index > 0) {
                        add(".%M(", MemberNames.chain)
                    }
                    add("%T()", converter.classType)
                    if (index > 0) {
                        add(")")
                    }
                }

            add("}")
        }
        .build()
}
