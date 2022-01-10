package com.dbflow5.model

import com.dbflow5.model.interop.OriginatingFileType
import com.dbflow5.model.properties.DatabaseHolderProperties

/**
 * Description:
 */
data class DatabaseHolderModel(
    val name: NameModel,
    val databases: List<DatabaseModel>,
    val properties: DatabaseHolderProperties,
    val allOriginatingFiles: List<OriginatingFileType>,
)
