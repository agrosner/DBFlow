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
        ksName: KSName,
        packageName: KSName,
        nullable: Boolean = false,
    ) : this(
        packageName = packageName.asString(),
        shortName = ksName.getShortName(),
        nullable = nullable,
    )

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
}
