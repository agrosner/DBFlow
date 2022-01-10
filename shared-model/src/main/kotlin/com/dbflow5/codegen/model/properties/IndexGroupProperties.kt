package com.dbflow5.codegen.model.properties

data class IndexGroupProperties(
    val number: Int,
    override val name: String,
    val unique: Boolean
) : NamedProperties
