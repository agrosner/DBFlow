package com.grosner.dbflow5.codegen.kotlin.kotlinpoet

import com.dbflow5.codegen.shared.PackageNames
import com.squareup.kotlinpoet.MemberName

object MemberNames {

    val property = MemberName(PackageNames.Property, "property")
    val typeConvertedProperty = MemberName(PackageNames.Property, "typeConvertedProperty")
    val quoteIfNeeded = MemberName(PackageNames.Core, "quoteIfNeeded")
    val infer = MemberName(PackageNames.Property, "infer")
    val classToken = MemberName(PackageNames.Property, "classToken")
    val indexProperty = MemberName(PackageNames.Property, "indexProperty")

    val bind = MemberName(PackageNames.Database, "bind")
    val propertyBind = MemberName(PackageNames.Property, "bindProperty")

    val select = MemberName(PackageNames.Query, "select")
    const val from = "from"
    const val where = "where"
    const val requireSingle = "requireSingle"
    const val querySingle = "querySingle"
    const val queryList = "queryList"
    const val eq = "eq"

    val save = MemberName(PackageNames.Structure, "save")
    val chain = MemberName(PackageNames.Converter, "chain")

    val getOrThrow = MemberName("kotlin", "getOrThrow")
}