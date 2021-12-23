package com.dbflow5.ksp.model

import com.dbflow5.ksp.model.properties.TypeConverterProperties
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.TypeName

/**
 * Description:
 */
data class TypeConverterModel(
    val name: NameModel,
    val properties: TypeConverterProperties,
    val classType: TypeName,
    val dataTypeName: TypeName,
    val modelTypeName: TypeName,
    val modelClass: KSClassDeclaration?,
) : ObjectModel
