package com.grosner.dbflow5.codegen.kotlin.writer

import com.dbflow5.codegen.model.OneToManyModel
import com.dbflow5.codegen.model.interop.OriginatingFileTypeSpecAdder
import com.dbflow5.codegen.writer.TypeCreator
import com.grosner.dbflow5.codegen.kotlin.kotlinpoet.ParameterPropertySpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec

/**
 * Description:
 */
class OneToManyClassWriter(
    private val originatingFileTypeSpecAdder: OriginatingFileTypeSpecAdder,
) : TypeCreator<OneToManyModel, FileSpec> {

    override fun create(model: OneToManyModel): FileSpec {
        val classModel = model.classModel
        return FileSpec.builder(
            classModel.name.packageName,
            classModel.name.shortName
        )
            .apply {
                val paramProperties = classModel.fields
                    .map { prop ->
                        ParameterPropertySpec(
                            name = prop.propertyName,
                            type = prop.classType,
                        )
                    }
                addType(TypeSpec.classBuilder(
                    classModel.name.shortName,
                )
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameters(paramProperties.map { it.parameterSpec })
                            .build()
                    )
                    .apply {
                        model.originatingFile?.let {
                            originatingFileTypeSpecAdder.addOriginatingFileType(this, it)
                        }
                        addModifiers(KModifier.DATA)
                        addProperties(paramProperties.map { it.propertySpec })
                    }
                    .build()
                )
            }
            .build()
    }
}