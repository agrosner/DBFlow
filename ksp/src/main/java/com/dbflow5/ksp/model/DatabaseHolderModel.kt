package com.dbflow5.ksp.model

import com.dbflow5.ksp.model.properties.DatabaseHolderProperties

/**
 * Description:
 */
data class DatabaseHolderModel(
    val name: NameModel,
    val databases: List<DatabaseModel>,
    val properties: DatabaseHolderProperties,
)
