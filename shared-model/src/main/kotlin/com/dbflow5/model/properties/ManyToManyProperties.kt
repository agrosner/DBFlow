package com.dbflow5.model.properties

import com.squareup.kotlinpoet.ClassName

data class ManyToManyProperties(
    val referencedTableType: ClassName,
    val referencedTableColumnName: String,
    val thisTableColumnName: String,
    val generateAutoIncrement: Boolean,
    val saveForeignKeyModels: Boolean,
    /**
     * Name of the generated class if overridden.
     */
    override val name: String,
) : NamedProperties
