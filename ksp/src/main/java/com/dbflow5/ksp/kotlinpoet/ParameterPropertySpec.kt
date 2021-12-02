package com.dbflow5.ksp.kotlinpoet

import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName

/**
 * Description: helps construct matching [Prop]
 */
data class ParameterPropertySpec(
    val name: String,
    val type: TypeName,
    val propertyConfig: PropertySpec.Builder.() -> Unit = {},
    val parameterConfig: ParameterSpec.Builder.() -> Unit = {},
) {
    val parameterSpec: ParameterSpec = ParameterSpec.builder(
        name = name,
        type = type
    ).apply(parameterConfig)
        .build()

    val propertySpec: PropertySpec = PropertySpec.builder(
        name = name,
        type = type,
    ).initializer(name)
        .apply(propertyConfig)
        .build()
}