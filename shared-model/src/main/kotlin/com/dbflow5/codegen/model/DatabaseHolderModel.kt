package com.dbflow5.codegen.model

import com.dbflow5.codegen.model.interop.OriginatingFileType
import com.dbflow5.codegen.model.properties.DatabaseHolderProperties

/**
 * Description:
 */
data class DatabaseHolderModel(
    val name: NameModel,
    val databases: List<DatabaseModel>,
    val properties: DatabaseHolderProperties,
    val allOriginatingFiles: List<OriginatingFileType>,
)
