package com.dbflow5.ksp.writer

import com.dbflow5.ksp.ClassNames
import com.dbflow5.ksp.model.interop.ksFile
import com.dbflow5.model.TypeConverterModel
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
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
                    model.originatingFile?.ksFile()?.let { addOriginatingKSFile(it) }
                    superclass(
                        ClassNames.TypeConverter
                            .parameterizedBy(
                                model.dataTypeName,
                                model.modelTypeName
                            )
                    )
                    val singleInlineField =
                        model.modelClass?.properties?.first()
                            ?: throw IllegalStateException("ModelClass not set on declaration.")
                    addFunction(
                        FunSpec.builder("getDBValue")
                            .addModifiers(KModifier.OVERRIDE)
                            .returns(model.dataTypeName)
                            .addParameter(ParameterSpec("model", model.modelTypeName))
                            .addStatement(
                                "return model.%L",
                                singleInlineField.simpleName.shortName
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
                                singleInlineField.simpleName.shortName,
                                "data"
                            )
                            .build()
                    )
                }
                .build())
            .build()
    }
}