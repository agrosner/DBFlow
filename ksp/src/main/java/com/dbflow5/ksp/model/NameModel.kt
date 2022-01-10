package com.dbflow5.ksp.model

import com.dbflow5.model.NameModel
import com.google.devtools.ksp.symbol.KSName

operator fun NameModel.Companion.invoke(
    simpleName: KSName,
    packageName: KSName,
    nullable: Boolean = false,
) = NameModel(
    packageName = packageName.asString(),
    shortName = simpleName.getShortName(),
    nullable = nullable,
)

val NameModel.ksName
    get() = object : KSName {
        override fun asString(): String = "$packageName.$shortName"

        override fun getQualifier(): String = packageName

        override fun getShortName(): String = shortName
    }

fun NameModel.companion() = copy(
    shortName = "$shortName.Companion"
)
