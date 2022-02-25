package com.dbflow5.processor.interop

import com.dbflow5.codegen.shared.ClassAdapterFieldModel
import com.dbflow5.codegen.shared.validation.ValidationException
import com.grosner.kpoet.typeName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.javapoet.toKTypeName
import javax.lang.model.element.VariableElement

fun adapterParamsForExecutableParams(
    parameters: List<VariableElement>,
    allowedParameterType: (typeName: ParameterizedTypeName) -> Boolean,
): List<ClassAdapterFieldModel> = parameters.map { element ->
    val type = element.asType().typeName.toKTypeName()
    if (type !is ParameterizedTypeName || !allowedParameterType(type)) {
        throw ValidationException("Invalid Parameter Type $type")
    } else {
        ClassAdapterFieldModel(
            name = element.name(),
            typeName = type,
        )
    }
}