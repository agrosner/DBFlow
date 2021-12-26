package com.dbflow5.ksp.model

import com.dbflow5.ksp.model.properties.MigrationProperties
import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.ClassName

/**
 * Description: Migration type.
 */
data class MigrationModel(
    val name: NameModel,
    val classType: ClassName,
    val properties: MigrationProperties,
    override val originatingFile: KSFile?,
) : ObjectModel
