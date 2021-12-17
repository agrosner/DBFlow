package com.dbflow5.ksp.model

import com.dbflow5.ksp.model.properties.TypeConverterProperties
import com.squareup.kotlinpoet.TypeName

/**
 * Description:
 */
data class TypeConverterModel(
    val name: NameModel,
    val properties: TypeConverterProperties,
    val classType: TypeName,
    val dataClassType: TypeName,
    val modelClassType: TypeName,
) : ObjectModel
