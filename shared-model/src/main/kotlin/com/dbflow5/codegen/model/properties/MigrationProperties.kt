package com.dbflow5.codegen.model.properties

import com.squareup.kotlinpoet.TypeName

data class MigrationProperties(
    val version: Int,
    val database: TypeName,
    val priority: Int,
)
