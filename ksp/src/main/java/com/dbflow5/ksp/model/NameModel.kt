package com.dbflow5.ksp.model

import com.google.devtools.ksp.symbol.KSName
import com.squareup.kotlinpoet.ClassName

/**
 * Description:
 */
data class NameModel(
    val packageName: String,
    val shortName: String,
    val nullable: Boolean = false,
) {
    constructor(
        simpleName: KSName,
        packageName: KSName,
        nullable: Boolean = false,
    ) : this(
        packageName = packageName.asString(),
        shortName = simpleName.getShortName(),
        nullable = nullable,
    )

    constructor(className: ClassName) : this(
        packageName = className.packageName,
        shortName = className.simpleName,
    )

    val ksName: KSName = object : KSName {
        override fun asString(): String = "$packageName.$shortName"

        override fun getQualifier(): String = packageName

        override fun getShortName(): String = shortName
    }

    val className = ClassName(packageName, shortName)

    val accessName = if (nullable) {
        "${shortName}?"
    } else {
        shortName
    }
}

fun NameModel.companion() = copy(
    shortName = "$shortName.Companion"
)
