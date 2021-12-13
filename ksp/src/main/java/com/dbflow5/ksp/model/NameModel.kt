package com.dbflow5.ksp.model

import com.google.devtools.ksp.symbol.KSName
import com.squareup.kotlinpoet.ClassName

/**
 * Description:
 */
data class NameModel(
    val packageName: String,
    val shortName: String
) {
    constructor(
        ksName: KSName,
        packageName: KSName
    ) : this(
        packageName = packageName.asString(),
        shortName = ksName.getShortName(),
    )

    constructor(className: ClassName) : this(
        packageName = className.packageName,
        shortName = className.simpleName,
    )

    val className = ClassName(packageName, shortName)
}
