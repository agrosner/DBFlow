package com.dbflow5.codegen.shared

import com.dbflow5.codegen.shared.interop.OriginatingFileType
import com.dbflow5.codegen.shared.properties.DatabaseHolderProperties

/**
 * Description:
 */
data class DatabaseHolderModel(
    val name: NameModel,
    val databases: List<DatabaseModel>,
    val properties: DatabaseHolderProperties,
    val allOriginatingFiles: List<OriginatingFileType>,
)
