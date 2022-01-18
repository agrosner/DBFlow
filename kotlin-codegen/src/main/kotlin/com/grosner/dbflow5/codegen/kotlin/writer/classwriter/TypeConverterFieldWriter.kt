package com.grosner.dbflow5.codegen.kotlin.writer.classwriter

import com.dbflow5.codegen.model.TypeConverterModel
import com.dbflow5.codegen.writer.TypeCreator
import com.squareup.kotlinpoet.PropertySpec


class TypeConverterFieldWriter : TypeCreator<TypeConverterFieldWriter.Input,
    PropertySpec> {

    data class Input(
        val typeConverterModel: TypeConverterModel,
        val fieldName: String,
    )

    override fun create(model: Input): PropertySpec {
        val converterModel = model.typeConverterModel
        return PropertySpec.builder(model.fieldName, converterModel.classType)
            .initializer("%T()", converterModel.classType)
            .build()
    }
}