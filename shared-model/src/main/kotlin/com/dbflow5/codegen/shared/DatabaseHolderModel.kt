package com.dbflow5.codegen.shared

import com.dbflow5.codegen.shared.interop.OriginatingSourceCollection
import com.dbflow5.codegen.shared.properties.DatabaseHolderProperties

/**
 * Description:
 */
data class DatabaseHolderModel(
    val name: NameModel,
    val databases: List<DatabaseModel>,
    val tables: List<ClassModel>,
    val queries: List<ClassModel>,
    val views: List<ClassModel>,
    val properties: DatabaseHolderProperties,
    override val originatingSource: OriginatingSourceCollection,
) : ObjectModel
