package com.grosner.dbflow5.codegen.kotlin.kotlinpoet

import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName

/**
 * Description: helps construct matching [Prop]
 */
data class ParameterPropertySpec(
    val name: String,
    val type: TypeName,
    /**
     * If true, split parameter from property
     */
    val useUnderscoreDifference: Boolean = true,
    val propertyConfig: PropertySpec.Builder.() -> Unit = {},
    val parameterConfig: ParameterSpec.Builder.() -> Unit = {},
) {
    private fun parameterName() = if (useUnderscoreDifference) "_$name" else name

    val parameterSpec: ParameterSpec = ParameterSpec.builder(
        name = parameterName(),
        type = type
    ).apply(parameterConfig)
        .build()

    val propertySpec: PropertySpec = PropertySpec.builder(
        name = name,
        type = type,
    ).initializer(parameterName())
        .apply(propertyConfig)
        .build()

}