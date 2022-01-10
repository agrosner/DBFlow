package com.dbflow5.codegen.model.properties

import com.squareup.kotlinpoet.ClassName

data class OneToManyProperties(
    val childTableType: ClassName,
    override val name: String,
    val parentFieldName: String,
    val childListFieldName: String,
) : NamedProperties
