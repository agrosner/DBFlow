package com.dbflow5.ksp.model.cache

import com.dbflow5.ksp.model.properties.NamedProperties

data class IndexGroupProperties(
    val number: Int,
    override val name: String,
    val unique: Boolean
) : NamedProperties
