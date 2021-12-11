package com.dbflow5.ksp.model

import com.dbflow5.ksp.model.properties.DatabaseProperties
import com.squareup.kotlinpoet.TypeName

/**
 * Description:
 */
data class DatabaseModel(
    val name: NameModel,
    val classType: TypeName,
    val properties: DatabaseProperties,
    val tables: List<ClassModel> = listOf(),
    val views: List<ClassModel> = listOf(),
    val queryModels: List<ClassModel> = listOf(),
) : ObjectModel


val DatabaseModel.generatedName
    get() = "${name.shortName}_Database"
