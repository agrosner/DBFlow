package com.dbflow5.ksp.model

import com.google.devtools.ksp.symbol.KSName

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
}
