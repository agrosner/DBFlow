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
    constructor(ksName: KSName) : this(
        ksName.getQualifier(),
        ksName.getShortName()
    )

    constructor(className: ClassName) : this(
        className.packageName,
        className.simpleName,
    )

    val className = ClassName(packageName, shortName)
}
