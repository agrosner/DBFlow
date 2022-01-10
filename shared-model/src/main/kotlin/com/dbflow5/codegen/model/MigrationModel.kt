package com.dbflow5.codegen.model

import com.dbflow5.codegen.model.interop.OriginatingFileType
import com.dbflow5.codegen.model.properties.MigrationProperties
import com.squareup.kotlinpoet.ClassName

/**
 * Description: Migration type.
 */
data class MigrationModel(
    val name: NameModel,
    val classType: ClassName,
    val properties: MigrationProperties,
    override val originatingFile: OriginatingFileType?,
) : ObjectModel
