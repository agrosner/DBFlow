package com.dbflow5.codegen.model

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

/**
 * Description:
 */
data class NameModel(
    val packageName: String,
    val shortName: String,
    val nullable: Boolean = false,
) {
    constructor(className: ClassName) : this(
        packageName = className.packageName,
        shortName = className.simpleName,
    )

    val className = ClassName(packageName, shortName)

    val accessName = if (nullable) {
        "${shortName}?"
    } else {
        shortName
    }

    companion object
}

fun NameModel.companion() = copy(
    shortName = "$shortName.Companion"
)
