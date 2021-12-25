package com.dbflow5.ksp.model

import com.dbflow5.ksp.model.properties.DatabaseHolderProperties
import com.google.devtools.ksp.symbol.KSFile

/**
 * Description:
 */
data class DatabaseHolderModel(
    val name: NameModel,
    val databases: List<DatabaseModel>,
    val properties: DatabaseHolderProperties,
    val allOriginatingFiles: List<KSFile>,
)
