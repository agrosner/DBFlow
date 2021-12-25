package com.dbflow5.ksp.writer

import com.dbflow5.ksp.ClassNames
import com.dbflow5.ksp.model.TypeConverterModel
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile

/**
 * Description:
 */
class InlineTypeConverterWriter : TypeCreator<
    TypeConverterModel, FileSpec> {
    override fun create(model: TypeConverterModel): FileSpec {
        return FileSpec.builder(model.name.packageName, model.name.shortName)
            .addType(TypeSpec.classBuilder(
                model.name.shortName,
            )
                .apply {
                    model.originatingFile?.let { addOriginatingKSFile(it) }
                    superclass(
                        ClassNames.TypeConverter
                            .parameterizedBy(
                                model.dataTypeName,
                                model.modelTypeName
                            )
                    )
                    val singleInlineField =
                        model.modelClass?.getAllProperties()?.first()
                            ?: throw IllegalStateException("ModelClass not set on declaration.")
                    addFunction(
                        FunSpec.builder("getDBValue")
                            .addModifiers(KModifier.OVERRIDE)
                            .returns(model.dataTypeName)
                            .addParameter(ParameterSpec("model", model.modelTypeName))
                            .addStatement(
                                "return model.%L",
                                singleInlineField.simpleName.getShortName()
                            )
                            .build()
                    )
                    addFunction(
                        FunSpec.builder("getModelValue")
                            .addModifiers(KModifier.OVERRIDE)
                            .returns(model.modelTypeName)
                            .addParameter(ParameterSpec("data", model.dataTypeName))
                            .addStatement(
                                "return %T(%L = %L)",
                                model.modelTypeName,
                                singleInlineField.simpleName.getShortName(),
                                "data"
                            )
                            .build()
                    )
                }
                .build())
            .build()
    }
}