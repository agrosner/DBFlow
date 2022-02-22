package com.dbflow5.codegen.shared

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName

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

    val className: ClassName by lazy {
        if (shortName.isEmpty()) {
            throw IllegalArgumentException("Null shortname ${this}")
        }
        ClassName(packageName, shortName).run {
            copy(
                nullable = this@NameModel.nullable,
                annotations = this.annotations,
                tags = this.tags,
            )
        }
    }

    val memberName: MemberName by lazy {
        MemberName(packageName, shortName)
    }

    val accessName = if (nullable) {
        "${shortName}?"
    } else {
        shortName
    }

    /**
     * Print FQN with "?" if nullable
     */
    fun print(): String {
        return "${packageName}.${shortName}${if (nullable) "?" else ""}"
    }

    companion object
}

fun NameModel.companion() = copy(
    shortName = "$shortName.Companion"
)
