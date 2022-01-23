package com.dbflow5.codegen.shared

import com.squareup.kotlinpoet.ClassName

/**
 * Description:
 */
data class NameModel(
    val packageName: String,
    val shortName: String,
    val nullable: Boolean,
) {
    constructor(className: ClassName) : this(
        packageName = className.packageName,
        shortName = className.simpleName,
        nullable = className.isNullable,
    )

    val className: ClassName = ClassName(packageName, shortName).run {
        copy(
            nullable = this@NameModel.nullable,
            annotations = this.annotations,
            tags = this.tags,
        )
    }

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
