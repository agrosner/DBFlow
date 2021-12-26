package com.dbflow5.ksp.model

import com.dbflow5.ksp.model.properties.DatabaseProperties
import com.google.devtools.ksp.symbol.KSFile
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
    val migrations: List<MigrationModel> = listOf(),
    override val originatingFile: KSFile?,
) : ObjectModel

val DatabaseModel.generatedClassName
    get() = NameModel(
        name.packageName,
        "${name.shortName}_Database"
    )
