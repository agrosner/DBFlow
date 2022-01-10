package com.dbflow5.model

import com.dbflow5.model.interop.OriginatingFileType
import com.dbflow5.model.properties.MigrationProperties
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
