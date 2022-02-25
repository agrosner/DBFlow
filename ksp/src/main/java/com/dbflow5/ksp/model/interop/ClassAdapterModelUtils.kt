package com.dbflow5.ksp.model.interop

import com.dbflow5.codegen.shared.ClassAdapterFieldModel
import com.dbflow5.codegen.shared.NameModel
import com.dbflow5.codegen.shared.validation.ValidationException
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ksp.toTypeName

fun adapterParamsForFunParams(
    parameters: List<KSValueParameter>,
    allowedParameterType: (typeName: ParameterizedTypeName) -> Boolean
): List<ClassAdapterFieldModel> {
    return parameters
        .map { value ->
            val typeName = value.type.toTypeName()
            if (typeName !is ParameterizedTypeName || !allowedParameterType(typeName)) {
                throw ValidationException("Invalid Parameter Type $typeName")
            } else {
                ClassAdapterFieldModel(
                    name = NameModel(
                        packageName = value.name!!.getQualifier(),
                        shortName = value.name!!.getShortName(),
                        nullable = false,
                    ),
                    typeName = typeName,
                )
            }
        }
}