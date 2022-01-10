package com.dbflow5.model

import com.squareup.kotlinpoet.ClassName

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
