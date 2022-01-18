package com.dbflow5.codegen.shared

import com.dbflow5.codegen.shared.interop.OriginatingFileType
import com.dbflow5.codegen.shared.properties.MigrationProperties
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
